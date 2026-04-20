package it.uniroma3.siw.siw_football.model;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.util.List;
import java.util.ArrayList;

@Entity
public class Tournament {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String name;

    private LocalDate startDate;

    // Relación: Un torneo tiene muchos equipos
    // 'mappedBy' indica que el dueño de la relación es el campo 'tournament' en la clase Team
    // 'cascade' asegura que si borramos un torneo, podemos decidir qué pasa con los equipos
    @OneToMany(mappedBy = "tournament", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Team> teams = new ArrayList<>();

    public Tournament() {}

    // Getters y Setters...
    public List<Team> getTeams() { return teams; }
    public void setTeams(List<Team> teams) { this.teams = teams; }
    // (Añade el resto de getters/setters del nombre y fechas)

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public LocalDate getDate() { return startDate; }
    public void setDate(LocalDate startDate) { this.startDate = startDate; }
}
