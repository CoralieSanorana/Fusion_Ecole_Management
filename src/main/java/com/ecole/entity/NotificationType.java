package com.ecole.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "notification_types")
public class NotificationType {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(unique = true, nullable = false)
    private String code;

    private String libelle;

    @Column(name = "template_message", columnDefinition = "TEXT")
    private String templateMessage;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getLibelle() {
        return libelle;
    }

    public void setLibelle(String libelle) {
        this.libelle = libelle;
    }

    public String getTemplateMessage() {
        return templateMessage;
    }

    public void setTemplateMessage(String templateMessage) {
        this.templateMessage = templateMessage;
    }
}
