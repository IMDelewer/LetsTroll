package dev.delewer.letstroll.menu;

import java.util.List;

public record Pagination<T>(List<T> items, int page, int pageSize) {

    public int totalPages() {
        if (items.isEmpty()) {
            return 1;
        }
        return (items.size() + pageSize - 1) / pageSize;
    }

    public int currentPage() {
        return Math.max(0, Math.min(page, totalPages() - 1));
    }

    public List<T> slice() {
        int from = currentPage() * pageSize;
        int to = Math.min(items.size(), from + pageSize);
        if (from >= to) {
            return List.of();
        }
        return items.subList(from, to);
    }

    public boolean hasPrevious() {
        return currentPage() > 0;
    }

    public boolean hasNext() {
        return currentPage() + 1 < totalPages();
    }
}
