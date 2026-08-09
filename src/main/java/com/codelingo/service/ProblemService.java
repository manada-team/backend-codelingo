package com.codelingo.service;

import com.codelingo.dto.*;
import com.codelingo.model.Problem;
import com.codelingo.model.ProblemProgress;
import com.codelingo.model.TestCase;
import com.codelingo.model.User;
import com.codelingo.repository.ProblemProgressRepository;
import com.codelingo.repository.ProblemRepository;
import com.codelingo.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProblemService {

    private final ProblemRepository problemRepository;
    private final ProblemProgressRepository problemProgressRepository;
    private final UserRepository userRepository;
    private final StreakService streakService;
    private final CodeExecutionService codeExecutionService;

    public List<ProblemResponse> getAllProblems() {
        return problemRepository.findAllByOrderByProblemNumberAsc()
                .stream().map(this::toResponse).collect(Collectors.toList());
    }

    public ProblemResponse getProblemById(Long id) {
        return toResponse(findProblem(id));
    }

    /**
     * Version para el panel de Admin: incluye TODOS los test cases (con su
     * flag hidden), a diferencia de la respuesta publica que solo expone
     * los visibles. Pensado para prellenar el formulario de edicion.
     */
    public ProblemAdminResponse getProblemForAdmin(Long id) {
        Problem problem = findProblem(id);
        ProblemAdminResponse r = new ProblemAdminResponse();
        r.setId(problem.getId());
        r.setProblemNumber(problem.getProblemNumber());
        r.setTitle(problem.getTitle());
        r.setDescription(problem.getDescription());
        r.setStarterCode(problem.getStarterCode());
        r.setDifficulty(problem.getDifficulty().name());
        r.setXpReward(problem.getXpReward());
        r.setTestCases(problem.getTestCases().stream()
                .sorted(Comparator.comparingInt(TestCase::getOrderIndex))
                .map(tc -> new TestCaseAdminView(tc.getInput(), tc.getExpectedOutput(), tc.isHidden()))
                .toList());
        return r;
    }

    @Transactional
    public ProblemResponse createProblem(ProblemRequest request) {
        Problem problem = Problem.builder()
                .problemNumber(request.getProblemNumber())
                .title(request.getTitle())
                .description(request.getDescription())
                .starterCode(request.getStarterCode())
                .difficulty(parseDifficulty(request.getDifficulty()))
                .xpReward(request.getXpReward() > 0 ? request.getXpReward() : 20)
                .build();

        problem.setTestCases(buildTestCases(request.getTestCases(), problem));

        return toResponse(problemRepository.save(problem));
    }

    @Transactional
    public ProblemResponse updateProblem(Long id, ProblemRequest request) {
        Problem problem = findProblem(id);

        problemRepository.findByProblemNumber(request.getProblemNumber())
                .filter(existing -> !existing.getId().equals(id))
                .ifPresent(existing -> {
                    throw new IllegalArgumentException(
                            "El número de problema " + request.getProblemNumber() + " ya está en uso"
                    );
                });

        problem.setProblemNumber(request.getProblemNumber());
        problem.setTitle(request.getTitle());
        problem.setDescription(request.getDescription());
        problem.setStarterCode(request.getStarterCode());
        problem.setDifficulty(parseDifficulty(request.getDifficulty()));
        problem.setXpReward(request.getXpReward() > 0 ? request.getXpReward() : 20);

        problem.getTestCases().clear();
        problem.getTestCases().addAll(buildTestCases(request.getTestCases(), problem));

        return toResponse(problemRepository.save(problem));
    }

    @Transactional
    public void deleteProblem(Long id) {
        problemRepository.delete(findProblem(id));
    }

    /**
     * Corre el codigo solo contra los test cases visibles (no ocultos), sin
     * persistir intentos ni progreso. Pensado para el boton "Ejecutar".
     */
    public RunResponse runSample(Long problemId, String code, String language) {
        Problem problem = findProblem(problemId);
        List<TestCase> sampleCases = problem.getTestCases().stream()
                .filter(tc -> !tc.isHidden())
                .sorted(Comparator.comparingInt(TestCase::getOrderIndex))
                .toList();

        if (sampleCases.isEmpty()) {
            return new RunResponse(false, 0, 0, List.of());
        }

        List<TestCaseResult> results = executeAgainst(sampleCases, code, language);
        long passed = results.stream().filter(TestCaseResult::isPassed).count();
        return new RunResponse(passed == results.size(), (int) passed, results.size(), results);
    }

    /**
     * Corre el codigo contra TODOS los test cases (incluidos los ocultos) y,
     * si pasan todos, marca el problema como completado y otorga XP.
     */
    @Transactional
    public SubmitResponse submit(User user, Long problemId, String code, String language) {
        Problem problem = findProblem(problemId);
        List<TestCase> allCases = problem.getTestCases().stream()
                .sorted(Comparator.comparingInt(TestCase::getOrderIndex))
                .toList();

        ProblemProgress progress = problemProgressRepository
                .findByUserAndProblemIdAndLanguage(user, problemId, language)
                .orElse(ProblemProgress.builder().user(user).problem(problem).language(language).build());

        List<TestCaseResult> results = executeAgainst(allCases, code, language);
        long passedCount = results.stream().filter(TestCaseResult::isPassed).count();
        boolean allPassed = !results.isEmpty() && passedCount == results.size();

        int xpEarned = 0;
        if (!progress.isCompleted()) {
            progress.setAttempts(progress.getAttempts() + 1);

            if (allPassed) {
                progress.setCompleted(true);
                progress.setCompletedAt(LocalDateTime.now());
                progress.setScore(calculateScore(problem, progress.getAttempts()));
                user.setTotalXp(user.getTotalXp() + problem.getXpReward());
                switch (language) {
                    case "python" -> user.setXpPython(user.getXpPython() + problem.getXpReward());
                    case "java" -> user.setXpJava(user.getXpJava() + problem.getXpReward());
                    case "c" -> user.setXpC(user.getXpC() + problem.getXpReward());
                }
                userRepository.save(user);
                streakService.registerActivity(user);
                xpEarned = problem.getXpReward();
            }
        }
        problemProgressRepository.save(progress);

        // No revelar input/output de los casos ocultos en la respuesta
        List<TestCaseResult> visibleResults = results.stream()
                .map(r -> r.isHidden() ? TestCaseResult.redacted(r.getCaseNumber(), r.isPassed()) : r)
                .toList();

        String message = allPassed
                ? (xpEarned > 0 ? "¡Correcto! Ganaste " + xpEarned + " XP." : "¡Correcto! Problema ya completado.")
                : "Pasaste " + passedCount + " de " + results.size() + " casos.";

        return new SubmitResponse(allPassed, (int) passedCount, results.size(), xpEarned, progress.getAttempts(), message, visibleResults);
    }

    private List<TestCaseResult> executeAgainst(List<TestCase> cases, String code, String language) {
        List<String> inputs = cases.stream().map(TestCase::getInput).toList();
        List<CodeExecutionService.TestCaseExecutionResult> execResults =
                codeExecutionService.executeBatch(code, language, inputs);

        List<TestCaseResult> results = new ArrayList<>();
        for (int i = 0; i < cases.size(); i++) {
            TestCase tc = cases.get(i);
            CodeExecutionService.TestCaseExecutionResult exec = execResults.get(i);
            boolean passed = exec.error() == null
                    && exec.output() != null
                    && exec.output().trim().equals(tc.getExpectedOutput().trim());

            results.add(new TestCaseResult(
                    i + 1,
                    passed,
                    tc.isHidden(),
                    tc.getInput(),
                    tc.getExpectedOutput(),
                    exec.error() != null ? exec.error() : exec.output(),
                    exec.stderr()
            ));
        }
        return results;
    }

    private List<TestCase> buildTestCases(List<TestCaseRequest> requests, Problem problem) {
        List<TestCase> cases = new ArrayList<>();
        int idx = 0;
        for (TestCaseRequest req : requests) {
            cases.add(TestCase.builder()
                    .input(normalizeNewlines(req.getInput()))
                    .expectedOutput(normalizeNewlines(req.getExpectedOutput()))
                    .hidden(req.isHidden())
                    .orderIndex(idx++)
                    .problem(problem)
                    .build());
        }
        return cases;
    }

    // Si alguien tipea o pega "\n" literal (backslash + n) en vez de un salto
    // de linea real al cargar un test case, lo normalizamos igual para que
    // el input llegue como espera el programa del jugador.
    private String normalizeNewlines(String value) {
        return value == null ? null : value.replace("\\r\\n", "\n").replace("\\n", "\n");
    }

    private Problem.Difficulty parseDifficulty(String value) {
        try {
            return Problem.Difficulty.valueOf(value.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Dificultad inválida: " + value + ". Usá EASY, MEDIUM o HARD.");
        }
    }

    private Problem findProblem(Long id) {
        return problemRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Problema no encontrado: " + id));
    }

    private int calculateScore(Problem problem, int attempts) {
        int base = problem.getXpReward() * 10;
        int penalty = (attempts - 1) * 10;
        return Math.max(base - penalty, problem.getXpReward());
    }

    private ProblemResponse toResponse(Problem problem) {
        ProblemResponse r = new ProblemResponse();
        r.setId(problem.getId());
        r.setProblemNumber(problem.getProblemNumber());
        r.setTitle(problem.getTitle());
        r.setDescription(problem.getDescription());
        r.setStarterCode(problem.getStarterCode());
        r.setDifficulty(problem.getDifficulty().name());
        r.setXpReward(problem.getXpReward());

        List<TestCaseView> samples = problem.getTestCases().stream()
                .filter(tc -> !tc.isHidden())
                .sorted(Comparator.comparingInt(TestCase::getOrderIndex))
                .map(tc -> new TestCaseView(tc.getInput(), tc.getExpectedOutput()))
                .toList();
        r.setSampleTestCases(samples);
        r.setTotalTestCases(problem.getTestCases().size());
        return r;
    }
}