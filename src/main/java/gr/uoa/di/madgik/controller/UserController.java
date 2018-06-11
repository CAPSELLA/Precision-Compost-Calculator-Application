package gr.uoa.di.madgik.controller;

import java.io.IOException;
import java.text.ParseException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nimbusds.jose.JOSEException;
import gr.uoa.di.madgik.model.Person;
import gr.uoa.di.madgik.model.Token;
import gr.uoa.di.madgik.interfaces.UserRepository;
import gr.uoa.di.madgik.repository.UserSpecs;
import gr.uoa.di.madgik.service.PasswordService;
import gr.uoa.di.madgik.utils.AuthUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;

import gr.uoa.di.madgik.model.Person;
import gr.uoa.di.madgik.service.UserService;


@RestController
@Component
@RequestMapping("/auth")
public class UserController {

    private static final ObjectMapper mapper = new ObjectMapper();

    @Autowired
    private UserService ldappersonService;

    @Value("${authorization.server}")
    String authenticationServer;

    @Value("${authorization.server.authenticateByUser}")
    String authenticationService;

    @Value("${facebook.clientId}")
    public String fbClientId;

    @Value("${facebook.secret}")
    public String fbSecret;

    @Value("${google.clientId}")
    public String googleClientId;
    @Value("${google.secret}")
    public String googleSecret;


    public static final String CLIENT_ID = "client_id", REDIRECT_URI = "redirect_uri",
            CLIENT_SECRET = "client_secret", CODE= "code", GRANT_TYPE = "grant_type",
            AUTH_CODE = "authorization_code", STATE="state";

    public static final String CONFLICT_MSG = "There is already a %s account that belongs to you",
            NOT_FOUND_MSG = "person not found", LOGING_ERROR_MSG = "Wrong email and/or password",
            UNLINK_ERROR_MSG = "Could not unlink %s account because it is your only sign-in method";
    @Resource
    UserRepository userRepository;

    @Autowired
    public void setuserRepository(UserRepository userRepository) {
        this.userRepository = userRepository;
    }
    @RequestMapping(value = "/login", method = RequestMethod.POST)
    @ResponseBody
    public ResponseEntity<?> login(@RequestBody Map<String,String> userMap,  HttpServletRequest request)
            throws JOSEException {
        Person person = new Person();
        person.setEmail(userMap.get("email"));
        person.setPassword(userMap.get("password"));

        final Optional<Person> foundUser = userRepository.findByEmail(person.getEmail());
        if (foundUser.isPresent()
                && PasswordService.checkPassword(person.getPassword(), foundUser.get().getPassword())) {
            final Token token = AuthUtils.createToken(request.getRemoteHost(), String.valueOf(foundUser.get().getId()));
            return ResponseEntity.status(HttpStatus.OK).body(token);
        }
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(LOGING_ERROR_MSG);
    }

    @RequestMapping(value = "/authenticate", method = RequestMethod.POST)
    @ResponseBody
    public ResponseEntity<?> authenticate(@RequestParam String personname, @RequestParam String password,
                                   HttpServletResponse response) throws IOException {

        Person person = new Person();

        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        MultiValueMap<String, String> params = new LinkedMultiValueMap<String, String>();
        params.add("personname", personname);
        params.add("password", password);
        ResponseEntity<?> entityResponse;
        UriComponents uriComponents = UriComponentsBuilder.fromHttpUrl(authenticationServer + authenticationService).queryParams(params).build();


        HttpEntity<String> entity = new HttpEntity<String>(headers);
        try {
            System.out.println("I'm in controller and the server is:" + authenticationServer + authenticationService);

            entityResponse = restTemplate.postForEntity(uriComponents.toUri(), entity, String.class);

            System.out.println(entityResponse);
            return entityResponse;
        } catch (HttpStatusCodeException exception) {

            return new ResponseEntity<String>(HttpStatus.INTERNAL_SERVER_ERROR);
        }

    }


    @RequestMapping(value = "/signup", method = RequestMethod.POST)
    @ResponseBody
    public ResponseEntity<?> register(@RequestBody Map<String,String> formMap, HttpServletRequest request) throws IOException, JOSEException {
        Person person = new Person();
        person.setEmail(formMap.get("email"));
        person.setPassword(PasswordService.hashPassword(formMap.get("password")));
        final Person savedperson = userRepository.save(person);
        final Token token = AuthUtils.createToken(request.getRemoteHost(), String.valueOf(savedperson.getId()));
        return ResponseEntity.status(HttpStatus.CREATED).body(token);
    }


    @RequestMapping(value = "/facebook", method = RequestMethod.POST)
    @ResponseBody
    public ResponseEntity<?> loginFacebook(@RequestBody Map<String,String> userMap,
                                           final HttpServletRequest request) throws JsonParseException, JsonMappingException,
            IOException, ParseException, JOSEException {
        Person person = new Person();
        person.setEmail(userMap.get("name"));
        person.setPassword(userMap.get("id"));
        person.setFacebook(userMap.get("id"));
        final Optional<Person> foundUser = userRepository.findByEmail(person.getEmail());
        if (foundUser.isPresent()
                && PasswordService.checkPassword(person.getPassword(), foundUser.get().getPassword())) {
            final Token token = AuthUtils.createToken(request.getRemoteHost(), String.valueOf(foundUser.get().getId()));
            return ResponseEntity.status(HttpStatus.OK).body(token);
        }
        else{
            person.setPassword(PasswordService.hashPassword(userMap.get("id")));

            final Person savedperson = userRepository.save(person);
            final Token token = AuthUtils.createToken(request.getRemoteHost(), String.valueOf(savedperson.getId()));
            return ResponseEntity.status(HttpStatus.CREATED).body(token);
        }
    }


    @RequestMapping(value = "/google", method = RequestMethod.POST)
    public ResponseEntity<?> loginGoogle(@RequestBody Map<String,String> userMap,
                                           final HttpServletRequest request) throws JsonParseException, JsonMappingException,
            IOException, ParseException, JOSEException {
        Person person = new Person();
        person.setEmail(userMap.get("email"));
        person.setPassword(userMap.get("id"));
        person.setGoogle(userMap.get("id"));
        final Optional<Person> foundUser = userRepository.findByEmail(person.getEmail());
        if (foundUser.isPresent()
                && PasswordService.checkPassword(person.getPassword(), foundUser.get().getPassword())) {
            final Token token = AuthUtils.createToken(request.getRemoteHost(), String.valueOf(foundUser.get().getId()));
            return ResponseEntity.status(HttpStatus.OK).body(token);
        }
        else{
            person.setPassword(PasswordService.hashPassword(userMap.get("id")));

            final Person savedperson = userRepository.save(person);
            final Token token = AuthUtils.createToken(request.getRemoteHost(), String.valueOf(savedperson.getId()));
            return ResponseEntity.status(HttpStatus.CREATED).body(token);
        }
    }


    private Map<String, Object> getResponseEntity(ResponseEntity response) throws JsonParseException,
            JsonMappingException, IOException {
        return mapper.readValue(response.toString(),
                new TypeReference<Map<String, Object>>() {
            });
    }


    private ResponseEntity<?> processperson(final HttpServletRequest request, final Person.Provider provider,
                                          final String id, final String email) throws JOSEException, ParseException {
        final List<Person> persons = userRepository.findAll(UserSpecs.findByProvider(provider, id));
        Person person = null;
        if (persons.size() > 0) {
            person = persons.get(0);
        }
        // Step 3a. If person is already signed in then link accounts.
        Person personToSave;
        final String authHeader = request.getHeader(AuthUtils.AUTH_HEADER_KEY);
        if (StringUtils.isNotBlank(authHeader)) {
            if (person != null) {
                return ResponseEntity.status(HttpStatus.CONFLICT).body(CONFLICT_MSG);
            }

            final String subject = AuthUtils.getSubject(authHeader);
            final Optional<Person> foundperson = userRepository.findById(Long.parseLong(subject));
            if (!foundperson.isPresent()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(NOT_FOUND_MSG);
            }

            personToSave = foundperson.get();
            personToSave.setProviderId(provider, id);
            if (personToSave.getEmail() == null) {
                personToSave.setEmail(email);
            }
            personToSave = userRepository.save(personToSave);
        } else {
            // Step 3b. Create a new person account or return an existing one.
            if (person != null) {
                personToSave = person;
            } else {
                personToSave = new Person();
                personToSave.setProviderId(provider, id);
                personToSave.setEmail(email);
                personToSave = userRepository.save(personToSave);
            }
        }

        final Token token = AuthUtils.createToken(request.getRemoteHost(), String.valueOf(personToSave.getId()));
        return ResponseEntity.status(HttpStatus.OK).body(token);
    }

    public static class Payload {
        String clientId;

        String redirectUri;

        String code;

        public String getClientId() {
            return clientId;
        }

        public String getRedirectUri() {
            return redirectUri;
        }

        public String getCode() {
            return code;
        }
    }


}
