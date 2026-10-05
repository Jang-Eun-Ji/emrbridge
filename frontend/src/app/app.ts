import { Component } from '@angular/core';
import { MatIconModule } from '@angular/material/icon';
import { MatSidenavModule } from '@angular/material/sidenav';
import { RouterLink, RouterLinkActive, RouterOutlet } from '@angular/router';

interface NavItem {
  path: string;
  label: string;
  caption: string;
  icon: string;
}

@Component({
  selector: 'app-root',
  imports: [MatIconModule, MatSidenavModule, RouterLink, RouterLinkActive, RouterOutlet],
  templateUrl: './app.html',
  styleUrl: './app.scss',
})
export class App {
  readonly nav: NavItem[] = [
    { path: '/dashboard', label: 'Dashboard', caption: 'Clinical statistics', icon: 'dashboard' },
    { path: '/patients', label: 'Patients', caption: 'Patient list', icon: 'group' },
    { path: '/conditions', label: 'Conditions', caption: 'Disease analysis', icon: 'medical_information' },
    { path: '/pipeline', label: 'Data Pipeline', caption: 'Processing status', icon: 'account_tree' },
  ];
}
