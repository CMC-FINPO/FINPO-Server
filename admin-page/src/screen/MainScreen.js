import * as React from 'react';
import { useEffect, useState } from 'react';
import { useNavigate, useSearchParams } from 'react-router-dom';
import UserCard from '../component/UserCard';
import ApiTestCard from '../component/ApiTestCard';
import Grid from '@mui/material/Grid';
import InterestRegionCard from '../component/InterestRegionCard';
import MyInterestRegionCard from '../component/MyInterestRegionCard';
import MyInterestCategoryCard from '../component/MyInterestCategoryCard';
import PolicyCard from '../component/PolicyCard';
import MyNotification from '../component/MyNotification';
import PostCard from '../component/PostCard';
import ReportCard from '../component/ReportCard';

export default function MainScreen({ user, setUser, fetch, fetchData }) {
  return (
    <div style={{ padding: 20 }}>
      {!user ? (
        <div>인증된 사용자만 확인할 수 있습니다</div>
      ) : (
        <Grid container spacing={3} sx={{ display: 'flex', justifyContent: 'center' }}>
          <Grid item xs='auto'>
            <UserCard fetch={fetch} fetchData={fetchData} />
          </Grid>
          <Grid item xs='auto'>
            <MyInterestRegionCard fetch={fetch} fetchData={fetchData} />
          </Grid>
          <Grid item xs='auto'>
            <MyInterestCategoryCard fetch={fetch} fetchData={fetchData} />
          </Grid>
          <Grid item xs='auto'>
            <PolicyCard fetch={fetch} fetchData={fetchData} />
          </Grid>
          <Grid item xs='auto'>
            <MyNotification fetch={fetch} fetchData={fetchData} />
          </Grid>
          <Grid item xs='auto'>
            <PostCard fetch={fetch} fetchData={fetchData} />
          </Grid>
          <Grid item xs='auto'>
            <ReportCard fetch={fetch} fetchData={fetchData} />
          </Grid>
          <Grid item xs='auto'>
            <ApiTestCard fetch={fetch} fetchData={fetchData} />
          </Grid>
        </Grid>
      )}
    </div>
  );
}
