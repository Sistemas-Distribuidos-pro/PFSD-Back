package com.monolito.ecommerce;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Order Service application.
 */
@SpringBootApplication
public class OrderServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(OrderServiceApplication.class, args);
		System.out.println("\n" +
				"╔════════════════════════════════════════════════════════════╗\n" +
				"║   ORDER SERVICE - PFSD Backend                            ║\n" +
				"║   Arquitectura: Servicio sincrono de ordenes              ║\n" +
				"║   Almacenamiento: En memoria (ConcurrentHashMap)          ║\n" +
				"║   Puerto: 8081                                            ║\n" +
				"╚════════════════════════════════════════════════════════════╝\n");
	}
}