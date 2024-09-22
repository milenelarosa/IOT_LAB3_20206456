package com.example.lab3_20206456.Services;

import java.util.List;

public class TaskResponse {
    private List<Task> todos;
    private int total;
    private int skip;
    private int limit;

    // Getters y setters
    public List<Task> getTodos() {
        return todos;
    }

    public void setTodos(List<Task> todos) {
        this.todos = todos;
    }

    public int getTotal() {
        return total;
    }

    public void setTotal(int total) {
        this.total = total;
    }

    public int getSkip() {
        return skip;
    }

    public void setSkip(int skip) {
        this.skip = skip;
    }

    public int getLimit() {
        return limit;
    }

    public void setLimit(int limit) {
        this.limit = limit;
    }
}

