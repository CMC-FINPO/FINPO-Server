import * as React from 'react';
import { useEffect, useState } from 'react';
import { useNavigate, useSearchParams } from 'react-router-dom';
import UserCard from '../component/UserCard';
import ApiTestCard from '../component/ApiTestCard';
import Grid from '@mui/material/Grid';
import InterestRegionCard from '../component/InterestRegionCard';
import MyInterestRegionCard from '../component/MyInterestRegionCard';

export default function MainScreen({ user, setUser }) {
  return (
    <div style={{ padding: 40 }}>
      {!user ? (
        <div>인증된 사용자만 확인할 수 있습니다</div>
      ) : (
        <Grid container spacing={3}>
          <Grid item xs='auto'>
            <UserCard />
          </Grid>
          <Grid item xs='auto'>
            <InterestRegionCard />
          </Grid>
          <Grid item xs='auto'>
            <MyInterestRegionCard />
          </Grid>

          <Grid item xs='auto'>
            <ApiTestCard />
          </Grid>
        </Grid>
      )}
    </div>
  );
}
