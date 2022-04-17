package com.bangular.main;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

@SpringBootApplication
@EnableSwagger2
public class MainApplication {

	public static void main(String[] args) {
		SpringApplication.run(MainApplication.class, args);
	}

//	@Bean
//	CommandLineRunner run(UserService userService) {
//		return args -> {
//			userService.saveRole(new Role(null, "ROLE_USER"));
//			userService.saveRole(new Role(null, "ROLE_MANAGER"));
//			userService.saveRole(new Role(null, "ROLE_ADMIN"));
//			userService.saveRole(new Role(null, "ROLE_SUPER_ADMIN"));
//
//			userService.saveUser(new User(null, "Oguz Tozkoparan", "dustbreaker", "oguztozkoparan@gmail.com", "1234", new ArrayList<>()));
//			userService.saveUser(new User(null, "Emre Erkan", "eerkan", "eerkan@hotmail.com", "1234", new ArrayList<>()));
//			userService.saveUser(new User(null, "Ufuk Sungu", "ufuck", "ufuck@icloud.com", "1234", new ArrayList<>()));
//			userService.saveUser(new User(null, "Deniz Dumanli", "denqza", "denqza@gmail.com", "1234", new ArrayList<>()));
//
//			userService.addRoleToUser("dustbreaker", "ROLE_SUPER_ADMIN");
//			userService.addRoleToUser("dustbreaker", "ROLE_ADMIN");
//			userService.addRoleToUser("dustbreaker", "ROLE_MANAGER");
//			userService.addRoleToUser("dustbreaker", "ROLE_USER");
//
//			userService.addRoleToUser("eerkan", "ROLE_USER");
//			userService.addRoleToUser("ufuck", "ROLE_USER");
//			userService.addRoleToUser("denqza", "ROLE_USER");
//		};
//	}

	@Bean
	PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}

}
