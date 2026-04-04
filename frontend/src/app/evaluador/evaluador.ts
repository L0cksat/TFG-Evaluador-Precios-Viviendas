import {
  Component,
  OnInit,
  AfterViewInit,
  ViewChild,
  ElementRef,
  ChangeDetectorRef,
} from '@angular/core';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule } from '@angular/forms';
import { InmobilarioBuscarService } from '../services/inmobilario-buscar';
import { HomeDataService } from '../services/home-data';
import { GeocodingService } from '../services/geocoding';
import * as L from 'leaflet';

@Component({
  selector: 'app-evaluador',
  standalone: true,
  imports: [ReactiveFormsModule],
  templateUrl: './evaluador.html',
  styleUrl: './evaluador.css',
})
export class EvaluadorComponent implements OnInit {
  formEvaluacion!: FormGroup;
  isLoading: boolean = false;
  resultadoValoracion: any = null; // Variable to hold the backend response

  constructor(
    private fb: FormBuilder,
    private ibs: InmobilarioBuscarService,
    private hds: HomeDataService,
    private geo: GeocodingService,
    private cdr: ChangeDetectorRef,
  ) {}

  // Add 'valuationMode' to your class properties
  valuationMode: 'basic' | 'pro' = 'basic';

  ngOnInit(): void {
    const addressPattern = /^[a-zA-ZñÑáéíóúÁÉÍÓÚ\s\.]+ \d+[a-zA-Z]?, [a-zA-ZñÑáéíóúÁÉÍÓÚ\s\d]+$/;
    // Catastral references in Spain are usually 20 alphanumeric characters
    const rcPattern = /^[0-9A-Z]{20}$/;

    this.formEvaluacion = this.fb.group({
      direccion: [this.hds.direccion, [Validators.required, Validators.pattern(addressPattern)]],
      referenciaCatastral: ['', [Validators.pattern(rcPattern)]], // New field
      metros: ['', [Validators.required, Validators.min(1)]],
      habitaciones: ['', [Validators.required, Validators.min(0)]],
    });
  }

  // Helper to switch modes and update validators
  setMode(mode: 'basic' | 'pro') {
    this.valuationMode = mode;
    const dirCtrl = this.formEvaluacion.get('direccion');
    const rcCtrl = this.formEvaluacion.get('referenciaCatastral');
    const metrosCtrl = this.formEvaluacion.get('metros');

    if (mode === 'pro') {
      rcCtrl?.setValidators([Validators.required, Validators.pattern(/^[0-9A-Z]{20}$/)]);
      dirCtrl?.clearValidators();
      metrosCtrl?.clearValidators();
    } else {
      rcCtrl?.clearValidators();
      dirCtrl?.setValidators([Validators.required, Validators.pattern(/^[a-zA-ZñÑáéíóúÁÉÍÓÚ\s\.]+ \d+[a-zA-Z]?, [a-zA-ZñÑáéíóúÁÉÍÓÚ\s\d]+$/)]); // use your pattern
      metrosCtrl?.setValidators([Validators.required, Validators.min(1)]);
    }

    rcCtrl?.updateValueAndValidity();
    dirCtrl?.updateValueAndValidity();
    metrosCtrl?.updateValueAndValidity();
  }

  // frontend/src/app/evaluador/evaluador.ts

  onSubmit() {
    if (this.formEvaluacion.valid) {
      this.isLoading = true;
      this.resultadoValoracion = null;
      this.cdr.detectChanges();

      const formValues = this.formEvaluacion.value;
      let payload: any;

      if (this.valuationMode === 'basic') {
        payload = {
          direccion: formValues.direccion,
          metrosCuadrados: formValues.metros,
          habitaciones: formValues.habitaciones,
        };
      } else {
        payload = {
          referenciaCatastral: formValues.referenciaCatastral,
          habitaciones: formValues.habitaciones,
        };
      }

      // Use the updated service method passing the payload object
      this.ibs.botInmobilario(payload).subscribe({
        next: (response) => {
          console.log('Valoración procesada exitosamente:', response);
          this.resultadoValoracion = response;
          this.isLoading = false;
          this.cdr.detectChanges();
        },
        error: (err) => {
          console.error('Error al conectar con el backend:', err);
          this.isLoading = false;
          this.cdr.detectChanges();
        },
      });
    }
  }

  @ViewChild('map') set mapContainer(ele: ElementRef | undefined) {
    if (ele) {
      this.initMap(ele.nativeElement);
    }
  }

  private initMap(nativeElement: HTMLElement): void {
    const map = L.map(nativeElement).setView([40.40807688713441, -3.6971967296675787], 13);

    L.tileLayer('https://tile.openstreetmap.org/{z}/{x}/{y}.png', {
      maxZoom: 19,
      attribution: '&copy; <a href="http://openstreetmap.org/copyright">OpenStreetMap</a>',
    }).addTo(map);

    let popup = L.popup();
    map.on('click', (event: any) => {
      const lat = event.latlng.lat;
      const lng = event.latlng.lng;

      popup.setLatLng(event.latlng).setContent(`Lat: ${lat}, Lng: ${lng}`).openOn(map);

      this.geo.reverseGeocode(lat, lng).subscribe({
        next: (address) => {
          this.formEvaluacion.patchValue({ direccion: address });
        },
        error: (err) => {
          console.error('Geocoding error:', err);
        },
      });
    });
  }
}
