package com.example.scale;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.client.RestClient;

@SpringBootApplication
public class ScaleApplication {

    public static void main(String[] args) {
        SpringApplication.run(ScaleApplication.class, args);
    }
}

//cora iberkleid

@Controller
@ResponseBody
class VirtualThreadsController {

    private final RestClient http;

    VirtualThreadsController(RestClient.Builder http) {
        this.http = http.build();
    }

    @GetMapping("/delay")
    String delay() {
        var s = Thread.currentThread() + ":";
        var results = this.http
                .get()
                .uri("http://localhost:80/delay/5")
                .retrieve()
                .body(String.class);
        s += Thread.currentThread();
        IO.println(s);
        return results;
    }


}