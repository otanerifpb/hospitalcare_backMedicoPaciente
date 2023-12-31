package br.edu.ifpb.pdist.back.controller;

import br.edu.ifpb.pdist.back.model.Medico;
import br.edu.ifpb.pdist.back.model.Paciente;
import br.edu.ifpb.pdist.back.rabbitmq.Producer;
import br.edu.ifpb.pdist.back.repository.MedicoRepository;
import br.edu.ifpb.pdist.back.repository.PacienteRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

import org.springframework.ui.Model;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@RestController
//@CrossOrigin(origins = "https://hospitalview-8171b04dfdb4.herokuapp.com")
@RequestMapping("/medico")
public class MedicoController {

    @Autowired
    private Producer producer;

    @Autowired
    private MedicoRepository medicoRepository;

    @Autowired
    private PacienteRepository pacienteRepository;
   
    // Ativa o menu Médico na barra de navegação
    // @ModelAttribute("menu")
    // public String activeMenu(){
    //     return "medico";
    // }

    // Rota para acessar a lista pelo menu
    @Cacheable(value = "medicos")
    @RequestMapping(method = RequestMethod.GET)
    public List<Medico> listAll(ModelAndView mav) {
        List<Medico> opMedicos = medicoRepository.findAll();
        return opMedicos;
    } 

    // Rota para acessar a lista ao usar o REDIRECT
    @RequestMapping("/")
    public List<Medico> listAll(Model model) {
       return medicoRepository.findAll();
    }

    @Caching( evict = {
        @CacheEvict(value = "medicos", allEntries = true)
    })


    // Rota para acessar o formunário
    // @RequestMapping("/formMedico")
    // public ModelAndView getFormMedico(Medico medico, ModelAndView mav) {
    //     mav.addObject("medico", medico);
    //     mav.setViewName("medico/formMedico");
    //     return mav;
    // }

    // Rota para cadastrar um Médico no Sitema
    @RequestMapping(value="/save", method = RequestMethod.POST)
    public ResponseEntity<Medico> save(@RequestBody Medico medico, RedirectAttributes redAttrs) {
        Optional<Medico> OpMedico = medicoRepository.findByCrm(medico.getCrm());
        if (!OpMedico.isPresent()){       
            Medico novoMedico = medicoRepository.save(medico);
            return ResponseEntity.status(HttpStatus.CREATED).body(novoMedico); 
        }
        return null;
   }

    // Rota para preencer os dados do formunlário de atualização com dados do banco 
    @RequestMapping("/{id}")
    public Medico getMedicoById(@PathVariable(value = "id") Integer id, ModelAndView mav) {
        Optional<Medico> opMedico = medicoRepository.findById(id);
        if (opMedico.isPresent()) {
            Medico medico = opMedico.get();
            return medico;
        } 
        return null;
    }
    
    // Rota para atualizar um Médico na lista pelo formUpMedico
    @RequestMapping(value="/update", method = RequestMethod.POST)
    public ResponseEntity<Medico> update(@RequestBody Medico medico, RedirectAttributes redAttrs) {
        Medico upDateMedico = medicoRepository.save(medico);
        return ResponseEntity.status(HttpStatus.OK).body(upDateMedico); 
    }

    // Rota para deletar um Médico da lista
    @RequestMapping("/delete/{id}")
    public void excluirMedico(@PathVariable(value = "id") Integer id) {
        Optional<Medico> OpMedico = medicoRepository.findById(id);
        if (OpMedico.isPresent()){
            Medico medico = OpMedico.get();
            medicoRepository.delete(medico);
        }
    }

    // Rota para mensageria
    @GetMapping("/paciente/{id}")
    public void consultarPaciente(@PathVariable int id) {
        Paciente paciente = pacienteRepository.findById(id).get();
        System.out.println(paciente.getNome());
        try {
            String message = "O paciente " + " " + paciente.getNome() +" " + " Foi consultado"; 
            producer.message(message);
        } catch (Exception e) {
            // TODO: handle exception
        }
    }
}
