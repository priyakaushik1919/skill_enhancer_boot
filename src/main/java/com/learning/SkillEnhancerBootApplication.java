package com.learning;

import com.learning.repository.StudentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.modelmapper.ModelMapper;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
@RequiredArgsConstructor
public class SkillEnhancerBootApplication implements CommandLineRunner {

	private final StudentRepository studentRepository;
	public static void main(String[] args) {
		SpringApplication.run(SkillEnhancerBootApplication.class, args);
	}
	@Bean
	public ModelMapper modelMapper(){
		return new ModelMapper();
	}

	@Override
	public void run(String... args) throws Exception {
	//	System.out.println(studentRepository.findAllName());
	//	System.out.println(studentRepository.findAllEmail());
	//	System.out.println(studentRepository.findAllContact());
		System.out.println(studentRepository.findAllContact2());
	}
}
