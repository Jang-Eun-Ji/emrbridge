import { Component, computed, input } from '@angular/core';
import { MatIconModule } from '@angular/material/icon';

import { loadErrorMessage } from '../../core/api/load-state';

@Component({
  selector: 'app-load-error',
  imports: [MatIconModule],
  templateUrl: './load-error.html',
  styleUrl: './load-error.scss',
})
export class LoadError {
  readonly statusCode = input<number>();
  readonly endpoint = input.required<string>();
  readonly message = computed(() => loadErrorMessage(this.statusCode(), this.endpoint()));
}
