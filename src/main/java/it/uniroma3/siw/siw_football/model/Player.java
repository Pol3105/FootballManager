package it.uniroma3.siw.siw_football.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.util.Objects;
import org.springframework.format.annotation.DateTimeFormat;

@Entity
public class Player {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "El nombre es obligatorio")
    @Column(nullable = false)
    private String name;

    @NotBlank(message = "El apellido es obligatorio")
    @Column(nullable = false)
    private String surname;

    @NotNull(message = "La altura es obligatoria")
    @DecimalMin(value = "1.40", message = "Altura mínima: 1.40 m")
    @DecimalMax(value = "2.20", message = "Altura máxima: 2.20 m")
    @Column(nullable = false)
    private Double height;

    @NotNull(message = "La fecha de nacimiento es obligatoria")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate birthDate;

    @NotBlank(message = "La posición es obligatoria")
    private String position;

    @ManyToOne(fetch = FetchType.LAZY)
    private Team team;

    public Player() {}

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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Player player = (Player) o;
        return Objects.equals(name, player.name) && Objects.equals(surname, player.surname);
    }

    @Override
    public int hashCode() { return Objects.hash(name, surname); }
}
