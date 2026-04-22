package it.uniroma3.siw.siw_football.model;


import jakarta.persistence.*;

import java.time.LocalDate;
import java.util.Objects;

import org.springframework.format.annotation.DateTimeFormat;

@Entity
public class Player {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String surname;

    @Column(nullable = false)
    private Double height;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate birthDate;

    private String position;

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

    public Double getHeight() { return height; }
    public void setHeight(Double height) { this.height = height; }

    public LocalDate getBirthDate() { return birthDate; }
    public void setBirthDate(LocalDate birthDate) { this.birthDate = birthDate; }

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