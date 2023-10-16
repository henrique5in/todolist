package br.dev.henrique.todolist.task;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import br.dev.henrique.todolist.utils.Utils;
import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/tasks")
public class TaskController {
  @Autowired
  private ITaskRepository taskRepository;

  @PostMapping("/")
  public ResponseEntity create(@RequestBody TaskModel task, HttpServletRequest request){
    task.setUserId((UUID) request.getAttribute("userId"));

    var currentDate = LocalDateTime.now();
    if(currentDate.isAfter(task.getStartAt()) || currentDate.isAfter(task.getEndAt())){
      return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Start date / End date must be after current date");
    }

    if(task.getStartAt().isAfter(task.getEndAt())){
      return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Start date must be before end date");
    }

    var taskCreated = this.taskRepository.save(task);
    return ResponseEntity.status(HttpStatus.CREATED).body(taskCreated);
  }
 
  @GetMapping("/")
  public List<TaskModel> list(HttpServletRequest request){
    var tasks = this.taskRepository.findByUserId((UUID) request.getAttribute("userId"));
    return tasks;
  }

  @PutMapping("/{id}")
  public ResponseEntity update(@RequestBody TaskModel task, HttpServletRequest request, @PathVariable("id") UUID id){
    var taskFound = this.taskRepository.findById(id).orElse(null);
    if(taskFound==null){
      return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Tarefa não encontrada");
    }
    if(!taskFound.getUserId().equals((UUID) request.getAttribute("userId"))){
      return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Você não tem permissão para alterar esta tarefa");
    }
    Utils.copyNonNullProperties(task, taskFound);
    var newTask = this.taskRepository.save(taskFound);
    return ResponseEntity.created(null).body(newTask); 
  }
}
