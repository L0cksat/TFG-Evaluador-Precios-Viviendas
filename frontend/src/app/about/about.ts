import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';

interface TeamMember {
  nombre: string;
  puesto: string;
  descripcion: string;
  imagen: string;
  github?: string;
  website?: string;
  linkedin?: string;
}

@Component({
  selector: 'app-about',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './about.html',
  styleUrl: './about.css'
})
export class AboutComponent {
  team: TeamMember[] = [
    {
      nombre: 'Kyle Lamm',
      puesto: 'Desarrollador Full Stack',
      descripcion: 'Me llamo Kyle y hace poco decidí aceptar el reto de dar un giro a mi carrera y adentrarme en el apasionante mundo de la tecnología. Ahora mismo estoy terminando mi formación profesional en DAW con UNIR y estoy deseando dar por concluido este primer paso de mi trayectoria para ver adónde me lleva. En mi tiempo libre, me gusta cocinar platos deliciosos en casa y jugar al ajedrez online.',
      imagen: 'images/LammKyle.jpg',
      github: 'https://github.com/Keeper90L',
      linkedin: 'https://www.linkedin.com/in/kyle-lamm-2496242bb/'
    },
    {
      nombre: 'Stephen Nicholas Jones De Giorgi',
      puesto: 'Desarrollador Full Stack, creación de microservicio Python',
      descripcion: '¡Hola! Soy Stephen Nicholas Jones De Giorgi (Bueno me puedes llamar Nicky) y siempre he estado enamorado de la tecnología, también tomé la decisión de cambiar de aires y embarcar en la FP de DAW, que estoy en la fase final de la formación y con ganas de trabajar en este sector. Me encanta la escritura, los videojuegos, la programación y los deportes. Soy padre de dos hijos.',
      imagen: 'images/NickyJones.jpg',
      github: 'https://github.com/L0cksat',
      website: 'https://stephennicholasjones.com/'
    },
    {
      nombre: 'Juan Montiel Fernández',
      puesto: 'Desarrollador Full Stack - Diseñador del backend en Spring Boot',
      descripcion: 'Descripción sobre el diseño de la interfaz y la experiencia de usuario de Evaluty.',
      imagen: 'images/Juan1.jpg',
      linkedin: 'https://www.linkedin.com/in/xmontiel/'
    }
  ];
}