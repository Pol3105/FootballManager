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

    @Column(nullable = false)
    private String city;

    private Integer foundationYear;

    // Relación: Muchos equipos pertenecen a un Torneo, y viceversa
    @ManyToMany
    private List<Tournament> tournaments = new ArrayList<>();

    //Relacion : Un equipo esta formado por muchos jugadores
    @OneToMany(mappedBy = "team", fetch = FetchType.LAZY)
    private List<Player> players = new ArrayList<>();

    public Team() {}

    public List<Player> getPlayers() { return players; }
    public void setPlayers(List<Player> teams) { this.players = teams; }

    public List<Tournament> getTournaments() { return tournaments; }
    public void setTournaments(List<Tournament> tournaments) { this.tournaments = tournaments; }

    // Getters, Setters y Equals/HashCode basados en 'name'
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public Integer getFoundationYear() { return foundationYear; }
    public void setFoundationYear(Integer foundationYear) { this.foundationYear = foundationYear; }

    public String getCity() { return city; }
    public void setCity(String city) { this.city = city; }
    
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
