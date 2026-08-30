package dev.delewer.letstroll;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import dev.delewer.letstroll.menu.Pagination;
import org.junit.jupiter.api.Test;

class PaginationTest {

    private static Pagination<Integer> of(int items, int page, int pageSize) {
        return new Pagination<>(java.util.stream.IntStream.range(0, items).boxed().toList(), page, pageSize);
    }

    @Test
    void countsPagesWithARemainder() {
        assertEquals(3, of(25, 0, 10).totalPages());
        assertEquals(2, of(20, 0, 10).totalPages());
        assertEquals(1, of(0, 0, 10).totalPages());
    }

    @Test
    void clampsThePageIntoRange() {
        assertEquals(2, of(25, 9, 10).currentPage());
        assertEquals(0, of(25, -4, 10).currentPage());
    }

    @Test
    void slicesTheRequestedPage() {
        assertEquals(List.of(0, 1, 2), of(7, 0, 3).slice());
        assertEquals(List.of(6), of(7, 2, 3).slice());
        assertEquals(List.of(), of(0, 0, 3).slice());
    }

    @Test
    void reportsNeighbouringPages() {
        Pagination<Integer> first = of(7, 0, 3);
        assertFalse(first.hasPrevious());
        assertTrue(first.hasNext());

        Pagination<Integer> last = of(7, 2, 3);
        assertTrue(last.hasPrevious());
        assertFalse(last.hasNext());

        Pagination<Integer> only = of(2, 0, 3);
        assertFalse(only.hasPrevious());
        assertFalse(only.hasNext());
    }
}
