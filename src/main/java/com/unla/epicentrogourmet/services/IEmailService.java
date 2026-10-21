package com.unla.epicentrogourmet.services;

public interface IEmailService {
    void enviar(String destinatario,String asunto,String texto);
}
