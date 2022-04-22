import { Component, OnInit } from '@angular/core';
import { Apollo, gql } from 'apollo-angular';

@Component({
  selector: 'app-profile',
  templateUrl: './profile.component.html',
})
export class ProfileComponent implements OnInit {
  user: any | undefined;
  loading = true;
  error: any;

  constructor(private apollo: Apollo) {
  }

  ngOnInit(): void {
    this.apollo.watchQuery({
      query: gql`
      {
        getUserByUsername(username: "dustbreaker") {
          name
          username
          email
        }
      }`
    }).valueChanges.subscribe((result: any) => {
      this.user = result?.data?.getUserByUsername;
      this.loading = false;
      this.error = result.errors;
    });
  }

}
