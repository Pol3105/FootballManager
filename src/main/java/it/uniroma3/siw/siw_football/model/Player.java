package it.uniroma3.siw.siw_football.model;


import jakarta.persistence.*;
import java.util.Objects;

@Entity
public class Player {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String surname;

    private String position; // Ej: Forward, Midfielder, Defender...

    // Relación: Muchos jugadores pertenecen a un equipo
    @ManyToOne(fetch = FetchType.LAZY)
    private Team team;

    public Player() {}

    // --- Getters y Setters ---
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getSurname() { return surname; }
    public void setSurname(String surname) { this.surname = surname; }

    public String getPosition() { return position; }
    public void setPosition(String position) { this.position = position; }

    public Team getTeam() { return team; }
    public void setTeam(Team team) { this.team = team; }

    // --- Equals y HashCode (Basado en Nombre y Apellido para evitar duplicados) ---
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Player player = (Player) o;
        return Objects.equals(name, player.name) && Objects.equals(surname, player.surname);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, surname);
    }
}