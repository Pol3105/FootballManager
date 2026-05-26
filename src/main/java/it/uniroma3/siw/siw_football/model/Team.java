package it.uniroma3.siw.siw_football.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Entity
public class Team {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "El nombre del club es obligatorio")
    @Column(nullable = false)
    private String name;

    @NotBlank(message = "La ciudad es obligatoria")
    @Column(nullable = false)
    private String city;

    @NotNull(message = "El año de fundación es obligatorio")
    @Min(value = 1800, message = "El año debe ser posterior a 1800")
    @Max(value = 2100, message = "El año no es válido")
    private Integer foundationYear;

    @ManyToMany
    private List<Tournament> tournaments = new ArrayList<>();

    @OneToMany(mappedBy = "team", fetch = FetchType.LAZY)
    private List<Player> players = new ArrayList<>();

    public Team() {}

    public List<Player> getPlayers() { return players; }
    public void setPlayers(List<Player> teams) { this.players = teams; }
    public List<Tournament> getTournaments() { return tournaments; }
    public void setTournaments(List<Tournament> tournaments) { this.tournaments = tournaments; }
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
    public int hashCode() { return Objects.hash(name); }
}
