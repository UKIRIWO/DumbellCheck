import { Component, OnInit, inject, signal, computed } from '@angular/core';
import { DecimalPipe } from '@angular/common';
import { StatsApiService } from '../../../../core/services/stats-api.service';
import { StatsData, WeeklyActivity, VolumeByGroup, ExerciseFrequency, ExerciseBestLift } from '../../../../core/models/stats.model';

@Component({
  selector: 'app-stats-page',
  imports: [DecimalPipe],
  templateUrl: './stats-page.html',
  styleUrl: './stats-page.scss',
})
export class StatsPage implements OnInit {
  private readonly statsApi = inject(StatsApiService);

  readonly selectedMonths = signal(3);
  readonly loading = signal(false);
  readonly errorMessage = signal('');
  readonly stats = signal<StatsData | null>(null);

  readonly periodOptions = [
    { label: '1 mes', value: 1 },
    { label: '2 meses', value: 2 },
    { label: '3 meses', value: 3 },
  ];

  readonly maxWeeklyCount = computed(() => {
    const weeks = this.stats()?.actividadSemanal ?? [];
    return Math.max(1, ...weeks.map((w) => w.cantidad));
  });

  readonly maxGroupVolume = computed(() => {
    const groups = this.stats()?.volumenPorGrupo ?? [];
    return Math.max(1, ...groups.map((g) => g.volumenKg));
  });

  readonly maxFrequency = computed(() => {
    const exs = this.stats()?.ejerciciosMasFrecuentes ?? [];
    return Math.max(1, ...exs.map((e) => e.veces));
  });

  ngOnInit(): void {
    this.load();
  }

  selectPeriod(months: number): void {
    if (months === this.selectedMonths()) return;
    this.selectedMonths.set(months);
    this.load();
  }

  barHeight(cantidad: number): number {
    return Math.max(2, Math.round((cantidad / this.maxWeeklyCount()) * 72));
  }

  groupWidthPercent(volumenKg: number): number {
    return Math.round((volumenKg / this.maxGroupVolume()) * 100);
  }

  freqWidthPercent(veces: number): number {
    return Math.round((veces / this.maxFrequency()) * 100);
  }

  formatVolume(kg: number): string {
    if (kg >= 1000) return `${(kg / 1000).toFixed(1)} t`;
    return `${kg.toLocaleString('es-ES')} kg`;
  }

  private load(): void {
    this.loading.set(true);
    this.errorMessage.set('');
    this.statsApi.getMyStats(this.selectedMonths()).subscribe({
      next: (data) => {
        this.stats.set(data);
        this.loading.set(false);
      },
      error: () => {
        this.errorMessage.set('No se pudieron cargar las estadísticas.');
        this.loading.set(false);
      },
    });
  }
}
