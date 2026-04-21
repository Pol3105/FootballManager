package it.uniroma3.siw.siw_football.model;


import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.Objects;
import java.util.List;

@Entity
public class Team {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    private Integer foundationYear;

    // Relación: Muchos equipos pertenecen a un Torneo
    @ManyToOne(fetch = FetchType.LAZY)
    private Tournament tournament;

    //Relacion : Un equipo esta formado por muchos jugadores
    @OneToMany(mappedBy = "team", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Player> players = new ArrayList<>();

    public Team() {}

    public List<Player> getPlayers() { return players; }
    public void setPlayers(List<Player> teams) { this.players = teams; }

    // Getters, Setters y Equals/HashCode basados en 'name'
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public Integer getFoundationYear() { return foundationYear; }
    public void setFoundationYear(Integer foundationYear) { this.foundationYear = foundationYear; }
    public Tournament getTournament() { return tournament; }
    public void setTournament(Tournament tournament) { this.tournament = tournament; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Team team = (Team) o;
        return Objects.equals(name, team.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name);
    }
}
