package com.example.notes.controller;

import com.example.notes.model.Note;
import com.example.notes.model.User;
import com.example.notes.repository.NoteRepository;
import com.example.notes.repository.UserRepository;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class NotesController {

    private final NoteRepository noteRepository;
    private final UserRepository userRepository;

    @Value("${app.dev-mode:false}")
    private boolean devMode;

    public NotesController(
        NoteRepository noteRepository,
        UserRepository userRepository
    ) {
        this.noteRepository = noteRepository;
        this.userRepository = userRepository;
    }

    @GetMapping("/notes")
    public String listNotes(
        Model model,
        @AuthenticationPrincipal OAuth2User principal,
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "25") int size
    ) {
        User user = getUserFromPrincipal(principal);
        Pageable pageable = PageRequest.of(page, size);
        Page<Note> notesPage = noteRepository.findByUser(user, pageable);

        model.addAttribute("notes", notesPage.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", notesPage.getTotalPages());
        model.addAttribute("isAuthenticated", true);

        return "notes";
    }

    @GetMapping("/notes/new")
    public String showCreateForm(
        Model model,
        @AuthenticationPrincipal OAuth2User principal
    ) {
        if (principal == null) {
            return "redirect:/login";
        }

        model.addAttribute("note", new Note());
        model.addAttribute("isAuthenticated", true);

        return "create-note";
    }

    @PostMapping("/notes")
    public String createNote(
        @RequestParam String title,
        @RequestParam String content,
        @AuthenticationPrincipal OAuth2User principal
    ) {
        User user;
        if (devMode && principal == null) {
            user = getDefaultUser();
        } else {
            if (principal == null) {
                return "redirect:/login";
            }
            user = getUserFromPrincipal(principal);
        }

        Note note = new Note();
        note.setTitle(title);
        note.setContent(content);
        note.setCreatedAt(LocalDateTime.now());
        note.setUpdatedAt(LocalDateTime.now());
        note.setUser(user);
        noteRepository.save(note);
        return "redirect:/notes";
    }

    @GetMapping("/notes/{id}/edit")
    public String showEditForm(
        @PathVariable Long id,
        Model model,
        @AuthenticationPrincipal OAuth2User principal
    ) {
        User user;
        if (devMode && principal == null) {
            user = getDefaultUser();
        } else {
            if (principal == null) {
                return "redirect:/login";
            }
            user = getUserFromPrincipal(principal);
        }

        Note note = noteRepository
            .findById(id)
            .orElseThrow(
                () -> new IllegalArgumentException("Invalid note ID: " + id)
            );

        if (!note.getUser().equals(user)) {
            return "redirect:/notes";
        }

        model.addAttribute("note", note);
        model.addAttribute("isAuthenticated", true);
        return "edit-note";
    }

    @PostMapping("/notes/{id}")
    public String updateNote(
        @PathVariable Long id,
        @RequestParam String title,
        @RequestParam String content,
        @AuthenticationPrincipal OAuth2User principal
    ) {
        User user;
        if (devMode && principal == null) {
            user = getDefaultUser();
        } else {
            if (principal == null) {
                return "redirect:/login";
            }
            user = getUserFromPrincipal(principal);
        }

        Note note = noteRepository
            .findById(id)
            .orElseThrow(
                () -> new IllegalArgumentException("Invalid note ID: " + id)
            );

        if (!note.getUser().equals(user)) {
            return "redirect:/notes";
        }

        note.setTitle(title);
        note.setContent(content);
        note.setUpdatedAt(LocalDateTime.now());
        noteRepository.save(note);

        return "redirect:/notes/" + id + "/edit"; // Redirect back to the edit page
    }

    private User getDefaultUser() {
        return userRepository
            .findByUsername("nealmick")
            .orElseThrow(
                () -> new RuntimeException("User 'nealmick' not found")
            );
    }

    @PostMapping("/notes/{id}/delete")
    public String deleteNote(
        @PathVariable Long id,
        @AuthenticationPrincipal OAuth2User principal
    ) {
        if (principal == null) {
            return "redirect:/login";
        }

        Note note = noteRepository
            .findById(id)
            .orElseThrow(
                () -> new IllegalArgumentException("Invalid note ID: " + id)
            );

        User user = getUserFromPrincipal(principal);
        if (!note.getUser().equals(user)) {
            return "redirect:/notes";
        }

        noteRepository.deleteById(id);
        return "redirect:/notes";
    }

    @GetMapping("/notes/search")
    public String searchNotes(
        Model model,
        @AuthenticationPrincipal OAuth2User principal,
        @RequestParam(required = false) String searchTerm,
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "100") int size
    ) {
        User user = getUserFromPrincipal(principal);
        Pageable pageable = PageRequest.of(page, size);
        Page<Note> notesPage;

        if (searchTerm == null || searchTerm.isBlank()) {
            notesPage = noteRepository.findByUser(user, pageable);
        } else {
            notesPage =
                noteRepository.findByUserAndTitleContainingIgnoreCaseOrContentContainingIgnoreCase(
                    user,
                    searchTerm,
                    searchTerm,
                    pageable
                );
        }

        model.addAttribute("notes", notesPage.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", notesPage.getTotalPages());
        model.addAttribute("searchTerm", searchTerm);
        model.addAttribute("isAuthenticated", true);
        return "notes";
    }

    private User getUserFromPrincipal(OAuth2User principal) {
        String username;

        if (principal instanceof OidcUser oidcUser) {
            // For Google OAuth2 authentication
            username = oidcUser.getEmail();
        } else {
            // For other providers (e.g., GitHub)
            String providerName = principal.getAttribute("provider");
            if (providerName == null || providerName.equals("github")) {
                username = principal.getAttribute("login");
            } else {
                throw new RuntimeException("Unsupported OAuth2 provider");
            }
        }

        User user = userRepository
            .findByUsername(username)
            .orElseGet(() -> {
                User newUser = new User();
                newUser.setUsername(username);
                return userRepository.save(newUser);
            });
        return user;
    }
}
