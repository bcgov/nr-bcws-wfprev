import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, Router } from '@angular/router';
import { ResourcesRoutes } from 'src/app/utils';

@Component({
  selector: 'app-list',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './list.component.html',
  styleUrl: './list.component.scss'
})
export class ListComponent implements OnInit {

  projects = [
    { 
      projectNumber: 1,
      name: 'Forest Fuel Reduction Program', 
      budget: 500000, 
      description: 'Removing dead vegetation and trees to reduce fuel loads in forests.' 
    },
    { 
      projectNumber: 2,
      name: 'Community Fire Awareness Campaign', 
      budget: 120000, 
      description: 'Educating communities on fire safety and prevention techniques.' 
    },
    { 
      projectNumber: 3,
      name: 'Smart Sensor Network Installation', 
      budget: 750000, 
      description: 'Deploying IoT sensors to monitor temperature and humidity in fire-prone areas.' 
    },
    { 
      projectNumber: 4,
      name: 'Aerial Fire Patrols', 
      budget: 3000000, 
      description: 'Using drones and aircraft to monitor and respond to wildfire threats in real-time.' 
    }
  ];

  filteredProjects = this.projects; // Holds the projects to display

  constructor(
    private router: Router,
    private route: ActivatedRoute
  ) {
  }

  ngOnInit(): void {
    // Check if there's a projectNumber in the query parameters
    this.route.queryParams.subscribe(params => {
      const projectNumber = params['projectNumber'];
      if (projectNumber) {
        this.filterProjects(parseInt(projectNumber, 10));
      } else {
        this.filteredProjects = this.projects; // Show all projects if no query param
      }
    });
  }

  filterProjects(projectNumber: number) {
    // Filter the projects to show only the one with the matching projectNumber
    this.filteredProjects = this.projects.filter(
      project => project.projectNumber === projectNumber
    );
  }
  
  viewProjectDetails(project: any) {
    console.log(`Viewing details for: ${project.name}`);
    this.router.navigate([ResourcesRoutes.LIST], {
      queryParams: { projectNumber: project.projectNumber }
    });
  }
}
