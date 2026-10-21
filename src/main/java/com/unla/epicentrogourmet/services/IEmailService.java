package com.unla.epicentrogourmet.services;

public interface IEmailService {

	// Manda un correo de texto plano. Lanza IllegalArgumentException si algún dato no es válido
	void enviar(String destinatario, String asunto, String texto);
}
