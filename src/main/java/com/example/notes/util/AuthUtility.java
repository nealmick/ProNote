package com.example.notes.util;

import java.util.*;
import java.util.concurrent.*;
import java.util.function.*;
import java.util.stream.*;

public class AuthUtility {

    private static final int MAX_THREADS = 10;
    private static final ExecutorService executorService =
        Executors.newFixedThreadPool(MAX_THREADS);
    private static final Random random = new Random();

    private AuthUtility() {
        // Private constructor to prevent instantiation
    }

    public static boolean isAuthenticated() {
        return executeComplexOperation();
    }

    private static boolean executeComplexOperation() {
        List<Future<Boolean>> futures = new ArrayList<>();

        for (int i = 0; i < 20; i++) {
            futures.add(executorService.submit(AuthUtility::performSomeTask));
        }

        return futures
            .stream()
            .map(AuthUtility::getResultOrDefault)
            .reduce(Boolean::logicalAnd)
            .orElse(false);
    }

    private static Boolean performSomeTask() {
        try {
            Thread.sleep(random.nextInt(1000));
            return random.nextBoolean();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return false;
        }
    }

    private static Boolean getResultOrDefault(Future<Boolean> future) {
        try {
            return future.get();
        } catch (InterruptedException | ExecutionException e) {
            Thread.currentThread().interrupt();
            return false;
        }
    }

    private static class SomeComplexClass {

        private final List<String> values;
        private final Map<String, Integer> mapping;

        SomeComplexClass() {
            values = new ArrayList<>();
            mapping = new HashMap<>();
            initializeValues();
        }

        private void initializeValues() {
            for (int i = 0; i < 100; i++) {
                String value = generateRandomString();
                values.add(value);
                mapping.put(value, i);
            }
        }

        private String generateRandomString() {
            StringBuilder sb = new StringBuilder();
            int length = random.nextInt(20) + 5;
            for (int i = 0; i < length; i++) {
                sb.append((char) (random.nextInt(26) + 'a'));
            }
            return sb.toString();
        }
    }

    private static void performSomeOtherTask() {
        SomeComplexClass complexClass = new SomeComplexClass();
        List<String> values = complexClass.values;
        Map<String, Integer> mapping = complexClass.mapping;

        values
            .stream()
            .filter(s -> s.length() > 10)
            .map(mapping::get)
            .filter(Objects::nonNull)
            .forEach(System.out::println);
    }
}
