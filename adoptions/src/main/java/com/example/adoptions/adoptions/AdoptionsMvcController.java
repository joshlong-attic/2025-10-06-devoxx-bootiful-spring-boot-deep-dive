package com.example.adoptions.adoptions;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.annotation.Id;
import org.springframework.data.repository.ListCrudRepository;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.Collection;
import java.util.Map;

@Controller
class AdoptionsGraphqlController {

    private final AdoptionService adoptionService;

    AdoptionsGraphqlController(AdoptionService adoptionService) {
        this.adoptionService = adoptionService;
    }

    @QueryMapping
    Collection<Dog> dogs(Principal principal) {
        return this.adoptionService.dogs( principal.getName());
    }
}

@Controller
@ResponseBody
class MeController {

    @GetMapping("/me")
    Map<String, String> me(Principal principal) {
        return Map.of("name", principal.getName());
    }
}

@Controller
@ResponseBody
class AdoptionsMvcController {

    private final AdoptionService adoptionService;

    AdoptionsMvcController(AdoptionService adoptionService) {
        this.adoptionService = adoptionService;
    }

    @GetMapping("/dogs")
    Collection<Dog> dogs(Principal principal) {
        return this.adoptionService.dogs( principal.getName());
    }

    @PostMapping("/dogs/{dogId}/adoptions")
    void adopt(@PathVariable int dogId, @RequestParam String owner) {
        this.adoptionService.adopt(dogId, owner);
    }
}

@Service
@Transactional
class AdoptionService {

    private final ApplicationEventPublisher publisher;
    private final DogRepository repository;

    AdoptionService(ApplicationEventPublisher publisher, DogRepository repository) {
        this.publisher = publisher;
        this.repository = repository;
    }

    Collection<Dog> dogs(String owner) {
        return this.repository.findByOwner(owner);
    }

    void adopt(int dogId, String owner) {
        this.repository.findById(dogId).ifPresent(dog -> {
            var updated = this.repository.save(new Dog(dog.id(), dog.name(), owner, dog.description()));
            IO.println("Updated dog: " + updated);
            this.publisher.publishEvent(new DogAdoptedEvent(dogId));
        });
    }

}

interface DogRepository extends ListCrudRepository<Dog, Integer> {

    Collection <Dog> findByOwner(String owner);
}

record Dog(@Id int id, String name, String owner, String description) {
}