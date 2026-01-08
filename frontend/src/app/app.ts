import { Component, signal } from '@angular/core';
import { Header } from "./header/header";
import { Body } from "./body/body";
import { Footer } from "./footer/footer";

@Component({
  selector: 'app-root',
  imports: [Header, Body, Footer],
  templateUrl: './app.html',
  styleUrl: './app.css'
})
export class App {
  protected readonly title = signal('my-frontend');
}
