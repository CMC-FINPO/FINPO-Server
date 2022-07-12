import * as React from 'react';
import { useEffect, useState } from 'react';
import Pagination from '@mui/material/Pagination';
import TextField from '@mui/material/TextField';
import FavoriteIcon from '@mui/icons-material/Favorite';
import FavoriteBorderIcon from '@mui/icons-material/FavoriteBorder';
import BookmarkIcon from '@mui/icons-material/Bookmark';
import BookmarkBorderIcon from '@mui/icons-material/BookmarkBorder';
import ImageUploading from 'react-images-uploading';

import {
  Button,
  Checkbox,
  Input,
  IconButton,
  ButtonGroup,
  Modal,
  Paper,
  Select,
  Table,
  TableBody,
  TableCell,
  TableContainer,
  TableHead,
  TableRow,
  Avatar,
  ImageListItem,
  ImageList,
} from '@mui/material';
import { axiosInstance } from '../axiosInstance';
import { Box } from '@mui/system';

export default function ReportScreen({ user, setUser, fetch, fetchData }) {
  const [data, setData] = useState([]);
  const [open, setOpen] = useState(false);
  const handleOpen = () => setOpen(true);
  const handleClose = () => setOpen(false);
  const [reload, setReload] = useState(false);
  const reloadTrigger = () => setReload(!reload);
  const [images, setImages] = useState([]);
  const [imgs, setImgs] = useState([]);

  const [detailOpen, setDetailOpen] = useState(false);

  const [modifyMode, setModifyMode] = useState(false);
  const [modifyContent, setModifyContent] = useState('');

  const [post, setPost] = useState({});
  const [postId, setPostId] = useState(0);
  const [comments, setComments] = useState({});

  const [page, setPage] = useState(1);

  const [text, setText] = useState('');

  const [content, setContent] = useState('');
  const [anonymity, setAnonymity] = useState(false);

  const [commentAnnonimity, setCommentAnnonimity] = useState(false);
  const [commentContent, setCommentContent] = useState('');

  const [type, setType] = useState('/search');

  useEffect(() => {
    axiosInstance.get(`report/community?&page=${page - 1}&size=10&sort=id,desc`).then((res) => {
      setData({ ...res.data.data });
    });

    setModifyMode(false);
    setModifyContent('');
  }, [page, reload, fetch, detailOpen]);

  return (
    <div style={{ padding: 10, display: 'flex', gap: 8, flexDirection: 'column', alignItems: 'center' }}>
      {!user ? (
        <div>인증된 사용자만 확인할 수 있습니다</div>
      ) : (
        <>
          <div style={{ padding: 10, display: 'flex', gap: 30 }}>
            <div>
              <Input
                variant='filled'
                value={text}
                onChange={(e) => {
                  setText(e.target.value);
                }}
              ></Input>

              <Button variant='contained' onClick={() => reloadTrigger()}>
                검색
              </Button>
            </div>

            <Button variant='contained' onClick={() => setOpen(true)}>
              글 작성
            </Button>
          </div>

          <div style={{ width: '100%', display: 'flex', justifyContent: 'space-around' }}></div>
          <TableContainer component={Paper}>
            <Table sx={{ minWidth: '600px' }} aria-label='simple table'>
              <TableHead>
                <TableRow>
                  <TableCell align='center'>id</TableCell>
                  <TableCell align='center'>사유</TableCell>
                  <TableCell align='center'>글id</TableCell>
                  <TableCell align='center'>글내용</TableCell>
                  <TableCell align='center'>댓글id</TableCell>
                  <TableCell align='center'>댓글내용</TableCell>
                  <TableCell align='center'>작성자</TableCell>
                  <TableCell align='center'>신고자</TableCell>
                </TableRow>
              </TableHead>
              <TableBody>
                {data?.content?.map((row, idx) => (
                  <TableRow
                    key={idx}
                    sx={{ '&:last-child td, &:last-child th': { border: 0 } }}
                    onClick={(e) => {
                      e.stopPropagation();
                      setDetailOpen(true);
                    }}
                  >
                    <TableCell align='center'>{row.id}</TableCell>
                    <TableCell align='center'>{row.report.reason}</TableCell>
                    <TableCell align='center'>{row.post?.id}</TableCell>
                    <TableCell align='center'>{row.post?.content}</TableCell>
                    <TableCell align='center'>{row.comment?.id}</TableCell>
                    <TableCell align='center'>{row.comment?.content}</TableCell>
                    <TableCell align='center'>{row.post?.user?.nickname || row.comment?.user?.nickname}</TableCell>
                    <TableCell align='center'>{row.user?.nickname}</TableCell>
                  </TableRow>
                ))}
              </TableBody>
            </Table>
          </TableContainer>
          <Pagination
            count={data?.totalPages}
            page={page}
            onChange={(e, value) => {
              setPage(value);
            }}
            color='primary'
          />

          <Modal open={detailOpen} onClose={() => setDetailOpen(false)}>
            <Box sx={{ ...style }}>
              {post && (
                <>
                  <div style={{ display: 'flex', gap: 10, alignItems: 'center' }}>
                    <Avatar src={post.user?.profileImg} />
                    <div>
                      <div style={{ fontWeight: '800', fontSize: '20px' }}>
                        {post.isUserWithdraw ? '탈퇴한유저' : post.anonymity ? '익명' : post.user?.nickname}
                      </div>
                      <div style={{ fontWeight: '200', fontSize: '11px', marginTop: '-3px' }}>
                        {post.createdAt} {post.isModified && '(수정됨)'}
                      </div>
                    </div>
                    <div>
                      {post.isMine && (
                        <ButtonGroup>
                          <Button
                            variant='contained'
                            sx={{ marginBottom: 0.5 }}
                            onClick={(e) => {
                              e.stopPropagation();
                              setModifyMode(!modifyMode);
                            }}
                          >
                            {!modifyMode ? '수정' : '수정취소'}
                          </Button>
                          <Button
                            variant='contained'
                            sx={{ marginBottom: 0.5 }}
                            onClick={(e) => {
                              e.stopPropagation();
                              axiosInstance.delete(`post/${post.id}`).then((res) => {
                                fetchData();
                                setDetailOpen(false);
                              });
                            }}
                          >
                            삭제
                          </Button>
                        </ButtonGroup>
                      )}
                      <Button
                        sx={{ marginBottom: 0.5 }}
                        onClick={(e) => {
                          e.stopPropagation();
                          axiosInstance
                            .post(`post/${postId}/report`, { report: { id: 4 } })
                            .then((res) => {
                              alert(res.data.data.report.reason + '로 신고완료');
                            })
                            .catch((res) => {
                              alert(res.response.data.message);
                            });
                        }}
                      >
                        신고
                      </Button>
                    </div>
                  </div>
                  <div style={{ display: 'flex', gap: 10, alignItems: 'center' }}>
                    <div style={{ fontWeight: '500', fontSize: '17px', marginTop: '16px', width: '100%' }}>
                      {!modifyMode ? (
                        <>
                          {post.content}
                          <ImageList sx={{ width: '100%', height: 200 }} cols={imgs?.length || 0}>
                            {Boolean(imgs.length) &&
                              imgs.map((img) => (
                                <ImageListItem key={img.order}>
                                  <img src={`${img.img}`} loading='lazy' />
                                </ImageListItem>
                              ))}
                          </ImageList>
                          {Boolean(imgs?.length) &&
                            Object.entries(imgs).map(([key, value]) => (
                              <div>
                                {key}:{' ' + JSON.stringify(value)}
                              </div>
                            ))}
                        </>
                      ) : (
                        <>
                          <TextField
                            fullWidth
                            size='small'
                            label='수정할 내용'
                            id='fullWidth'
                            rows={5}
                            multiline
                            value={modifyContent}
                            onChange={(e) => {
                              setModifyContent(e.target.value);
                            }}
                          />
                          <div style={{ display: 'flex' }}>
                            {imgs.map((img) => (
                              <div style={{ position: 'relative' }}>
                                <div
                                  style={{
                                    position: 'absolute',
                                    zIndex: 100,
                                    top: '-2px',
                                    right: '-2px',
                                    width: '20px',
                                    borderRadius: '50%',
                                    height: '20px',
                                    backgroundColor: 'red',
                                    display: 'flex',
                                    alignItems: 'center',
                                    justifyContent: 'center',
                                  }}
                                >
                                  X
                                </div>
                                <img
                                  src={`${img.img}`}
                                  loading='lazy'
                                  style={{ width: '60px' }}
                                  onClick={() => {
                                    let temp = [...imgs.filter((e) => e.img !== img.img)];
                                    setImgs([...temp]);
                                  }}
                                />
                              </div>
                            ))}
                            <ImageUploading
                              multiple
                              value={images}
                              onChange={(imageList, addUpdateIndex) => {
                                setImages(imageList);
                              }}
                              maxNumber={5 - imgs.length}
                              dataURLKey='data_url'
                            >
                              {({ imageList, onImageUpload, onImageRemoveAll, onImageUpdate, onImageRemove, isDragging, dragProps }) => (
                                <div className='upload__image-wrapper'>
                                  <button style={isDragging ? { color: 'red' } : undefined} onClick={onImageUpload} {...dragProps}>
                                    Add image
                                  </button>
                                  &nbsp;
                                  {imageList.map((image, index) => (
                                    <div key={index} className='image-item'>
                                      <img src={image['data_url']} alt='' width='100' />
                                      <div className='image-item__btn-wrapper'>
                                        <button onClick={() => onImageUpdate(index)}>Update</button>
                                        <button onClick={() => onImageRemove(index)}>Remove</button>
                                      </div>
                                    </div>
                                  ))}
                                </div>
                              )}
                            </ImageUploading>
                          </div>
                          <Button
                            size='sx'
                            variant='contained'
                            onClick={() => {
                              const formData = new FormData();
                              images.forEach((image) => formData.append('imgFiles', image.file));

                              axiosInstance
                                .post(`upload/post`, formData, {
                                  headers: {
                                    'Content-Type': 'multipart/form-data',
                                  },
                                })
                                .then((res) => {
                                  let temp = [...imgs];
                                  console.log(temp);

                                  res.data.data.imgUrls.forEach((img, idx) => temp.push({ order: idx + 1, img }));

                                  temp.forEach((e, i) => {
                                    temp[i].order = i + 1;
                                  });
                                  console.log(temp);

                                  axiosInstance
                                    .put(`post/${post.id}`, { content: modifyContent, imgs: [...temp] })
                                    .then((res) => {
                                      fetchData();
                                    })
                                    .catch((res) => {
                                      alert(res.response.data.message);
                                    });
                                });
                            }}
                          >
                            수정
                          </Button>
                        </>
                      )}
                    </div>
                  </div>

                  <div style={{ display: 'flex', gap: 10, alignItems: 'center' }}>
                    <div style={{ fontWeight: '500', fontSize: '17px', marginTop: '16px', width: '100%' }}>
                      <>
                        익명
                        <Checkbox
                          checked={commentAnnonimity}
                          onChange={(e) => {
                            setCommentAnnonimity(e.target.checked);
                          }}
                        ></Checkbox>
                        <TextField
                          size='small'
                          label='댓글'
                          multiline
                          value={commentContent}
                          onChange={(e) => {
                            setCommentContent(e.target.value);
                          }}
                        />
                        <Button
                          size='sx'
                          variant='contained'
                          onClick={() => {
                            axiosInstance
                              .post(`post/${post.id}/comment`, { content: commentContent, anonymity: commentAnnonimity })
                              .then((res) => {
                                fetchData();
                              })
                              .catch((res) => {
                                alert(res.response.data.message);
                              });
                          }}
                        >
                          입력
                        </Button>
                      </>
                    </div>
                  </div>
                </>
              )}
            </Box>
          </Modal>

          <Modal open={open} onClose={handleClose} aria-labelledby='modal-modal-title' aria-describedby='modal-modal-description'>
            <Box sx={style}>
              <div style={{ width: '100%', display: 'flex', flexDirection: 'column', alignItems: 'center', gap: 20, marginTop: 30 }}>
                <div>
                  익명여부
                  <Checkbox
                    label='익명'
                    checked={anonymity}
                    onChange={(e) => {
                      setAnonymity(e.target.checked);
                    }}
                  ></Checkbox>
                </div>
                <TextField
                  label='내용'
                  value={content}
                  multiline
                  rows={5}
                  onChange={(e) => {
                    setContent(e.target.value);
                  }}
                ></TextField>

                <ImageUploading
                  multiple
                  value={images}
                  onChange={(imageList, addUpdateIndex) => {
                    setImages(imageList);
                  }}
                  maxNumber={5}
                  dataURLKey='data_url'
                >
                  {({ imageList, onImageUpload, onImageRemoveAll, onImageUpdate, onImageRemove, isDragging, dragProps }) => (
                    <div className='upload__image-wrapper'>
                      <button style={isDragging ? { color: 'red' } : undefined} onClick={onImageUpload} {...dragProps}>
                        Add image
                      </button>
                      &nbsp;
                      <button onClick={onImageRemoveAll}>Remove all images</button>
                      {imageList.map((image, index) => (
                        <div key={index} className='image-item'>
                          <img src={image['data_url']} alt='' width='100' />
                          <div className='image-item__btn-wrapper'>
                            <button onClick={() => onImageUpdate(index)}>Update</button>
                            <button onClick={() => onImageRemove(index)}>Remove</button>
                          </div>
                        </div>
                      ))}
                    </div>
                  )}
                </ImageUploading>

                <div style={{ width: '100%', display: 'flex', justifyContent: 'center', gap: 20 }}>
                  <Button
                    variant='contained'
                    sx={{ marginBottom: 0.5 }}
                    onClick={(e) => {
                      e.stopPropagation();
                      const formData = new FormData();
                      images.forEach((image) => formData.append('imgFiles', image.file));

                      axiosInstance
                        .post(`upload/post`, formData, {
                          headers: {
                            'Content-Type': 'multipart/form-data',
                          },
                        })
                        .then((res) => {
                          let imgs = [];
                          res.data.data.imgUrls.forEach((img, idx) => imgs.push({ order: idx + 1, img }));

                          axiosInstance
                            .post(`post`, { content, anonymity, imgs })
                            .then((res) => {
                              fetchData();
                              handleClose();
                            })
                            .catch((res) => {
                              alert(res.response.data.message);
                            });
                        })
                        .catch((res) => {
                          alert(res.response.data.message);
                        });
                    }}
                  >
                    글 작성
                  </Button>
                </div>
              </div>
            </Box>
          </Modal>
        </>
      )}
    </div>
  );
}

const style = {
  position: 'absolute',
  top: '50%',
  left: '50%',
  transform: 'translate(-50%, -50%)',
  bgcolor: 'background.paper',
  borderRadius: 4,
  boxShadow: 24,
  p: 4,
  maxWidth: '95vw',
  maxHeight: '95vh',
  overflow: 'auto',
};
