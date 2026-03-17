import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { Router } from '@angular/router';
import { ValuationService, ValuationResponse } from '../services/valuation';
import { DatePipe, CurrencyPipe } from '@angular/common';
import { AuthService } from '../services/auth.service';

@Component({
  selector: 'app-history',
  standalone: true,
  imports: [DatePipe, CurrencyPipe],
  templateUrl: './history.html',
  styleUrl: './history.css',
})
export class HistoryComponent implements OnInit {
  valuations: ValuationResponse[] = [];

  constructor(
    private valuationService: ValuationService,
    private router: Router,
    private cdr: ChangeDetectorRef,
    public authService: AuthService
  ) {}

  ngOnInit() {
    this.valuationService.getUserValuations().subscribe({
      next: (data) => {
        this.valuations = data
        this.cdr.detectChanges()
      }, 
      error: (err) => console.error('Error fetching history:', err),
    });
  }

  goToEvaluador() {
    this.router.navigate(['/evaluador']);
  }
}