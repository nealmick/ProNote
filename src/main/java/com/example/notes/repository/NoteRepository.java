package com.example.notes.repository;

import com.example.notes.model.Note;
import com.example.notes.model.User;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface NoteRepository extends JpaRepository<Note, Long> {
    @Query(
        "SELECT n FROM Note n WHERE n.user = :user ORDER BY n.createdAt DESC"
    )
    Page<Note> findByUser(User user, Pageable pageable);

    @Query(
        "SELECT n FROM Note n WHERE n.user = :user AND (LOWER(n.title) LIKE LOWER(CONCAT('%', :titleSearchTerm, '%')) OR LOWER(n.content) LIKE LOWER(CONCAT('%', :contentSearchTerm, '%'))) ORDER BY n.createdAt DESC"
    )
    Page<
        Note
    > findByUserAndTitleContainingIgnoreCaseOrContentContainingIgnoreCase(
        User user,
        String titleSearchTerm,
        String contentSearchTerm,
        Pageable pageable
    );
}
