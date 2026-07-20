package com.example.consultas.seeder;

import com.example.consultas.models.*;
import com.example.consultas.repositories.*;
import net.datafaker.Faker;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;

@Component
@Profile("seed")
public class DataSeeder implements CommandLineRunner {

    private static Faker faker = new Faker(Locale.of("pt-BR"));
    private static final List<String> BAIRROS = List.of(
            "Barra",
            "Pituba",
            "Brotas",
            "Rio Vermelho",
            "Itapuã",
            "Cabula",
            "Pernambués",
            "Imbuí",
            "Ondina",
            "Caminho das Árvores",
            "Graça",
            "Federação",
            "Stella Maris",
            "Paralela",
            "Liberdade"
    );
    private final MedicoRepository medicoRepository;
    private final UsuarioRepository usuarioRepository;
    private final EnderecoRepository enderecoRepository;
    private final PasswordEncoder passwordEncoder;
    private final HorarioRepository horarioRepository;
    private final ClienteRepository clienteRepository;
    private final ConsultaRepository consultaRepository;

    public DataSeeder(MedicoRepository medicoRepository, UsuarioRepository usuarioRepository, EnderecoRepository enderecoRepository, PasswordEncoder passwordEncoder, HorarioRepository horarioRepository, ClienteRepository clienteRepository, ConsultaRepository consultaRepository) {
        this.medicoRepository = medicoRepository;
        this.usuarioRepository = usuarioRepository;
        this.enderecoRepository = enderecoRepository;
        this.passwordEncoder = passwordEncoder;
        this.horarioRepository = horarioRepository;
        this.clienteRepository = clienteRepository;
        this.consultaRepository = consultaRepository;
    }

    @Override
    public void run(String... args) throws Exception {
        criarAdmin();
        criarClientesComConsultas();
    }

    private void criarAdmin(){
        UsuarioModel usuarioModel = new UsuarioModel(null, "admin@teste.com", passwordEncoder.encode("admin123"),Roles.ADMIN, true, null,null, null);
    }


    private void criarMedicos(){
        for (int i = 1; i <= 2500; i++) {

            UsuarioModel usuario = new UsuarioModel(null, "medico"+i+"@teste.com",passwordEncoder.encode("12345Me@"), Roles.MEDICO, true, null, null, null);
            usuarioRepository.save(usuario);
            MedicoModel medico = new MedicoModel(null, faker.name().fullName(), String.valueOf(i), "99999999999", "Clinico Geral",null, null, usuario);
            medicoRepository.save(medico);
            EnderecoModel endereco = new EnderecoModel(null, "BA", "Salvador", "99999999", faker.options().nextElement(BAIRROS), faker.address().streetName(),faker.address().streetAddressNumber(), true, usuario);
            enderecoRepository.save(endereco);
            horarioRepository.save(new HorarioMedico(null,DiaSemana.SEGUNDA, LocalTime.of(7, 30, 0,0),LocalTime.of(17, 30, 0,0),15, true, medico));
        }
    }

    private void criarClientesComConsultas(){
        Random random = new Random();
        HashSet<String> existeConsulta = new HashSet<>();
        List<MedicoModel> medicos = medicoRepository.findAll();

        for (int i = 1; i <= 2500; i++) {
            UsuarioModel usuario = new UsuarioModel(null, "cliente"+i+"@teste.com",passwordEncoder.encode("12345Cl@"), Roles.CLIENTE, true, null, null, null);
            usuarioRepository.save(usuario);
            ClienteModel cliente = new ClienteModel(null, faker.name().fullName(), String.valueOf(99999990000L+i), "99999999999", null, usuario);
            clienteRepository.save(cliente);

            for (int j = 0; j < 6; j++) {

                MedicoModel medico = medicos.get(random.nextInt(medicos.size()));
                LocalDateTime horaInicial = LocalDateTime.of(2026, 6, 15, 7, 30);
                while(consultaRepository.existsByMedico_IdAndDataConsulta(medico.getId(), horaInicial)){
                    horaInicial = horaInicial.plusMinutes(15);
                }
                ConsultaModel consulta = new ConsultaModel(null, horaInicial ,"Clinico Geral", "Clinico Geral", Status.PENDENTE, medico , cliente);
                consultaRepository.save(consulta);
            }
        }

    }
}
