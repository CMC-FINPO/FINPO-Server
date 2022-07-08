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

export default function PostScreen({ user, setUser, fetch, fetchData }) {
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
    axiosInstance.get(`post${type}?content=${text}&page=${page - 1}&size=10&sort=id,desc`).then((res) => {
      setData({ ...res.data.data });
    });

    setImages([]);
    setModifyMode(false);
    setModifyContent('');
  }, [page, reload, fetch, detailOpen]);

  useEffect(() => {
    reloadPost();
  }, [postId]);

  const reloadPost = () => {
    axiosInstance.get(`post/${postId}`).then((res) => {
      setPost(res.data.data);
      setModifyContent(res.data.data.content);
      let temp = res.data.data.imgs.sort((a, b) => a.order - b.order);
      setImgs([...temp]);
    });

    axiosInstance.get(`post/${postId}/comment?size=100`).then((res) => {
      setComments([...res.data.data.content]);
    });
  };

  return (
    <div style={{ padding: 10, display: 'flex', gap: 8, flexDirection: 'column', alignItems: 'center' }}>
      {!user ? (
        <div>인증된 사용자만 확인할 수 있습니다</div>
      ) : (
        <>
          <div style={{ padding: 10, display: 'flex', gap: 30 }}>
            <ButtonGroup>
              <Button
                variant='contained'
                onClick={(e) => {
                  setType('/search');
                  fetchData();
                }}
              >
                전체
              </Button>
              <Button
                variant='contained'
                onClick={(e) => {
                  setType('/me');
                  fetchData();
                }}
              >
                내가쓴글
              </Button>
              <Button
                variant='contained'
                onClick={(e) => {
                  setType('/like/me');
                  fetchData();
                }}
              >
                좋아요한글
              </Button>
              <Button
                variant='contained'
                onClick={(e) => {
                  setType('/bookmark/me');
                  fetchData();
                }}
              >
                북마크한글
              </Button>
              <Button
                variant='contained'
                onClick={(e) => {
                  setType('/comment/me');
                  fetchData();
                }}
              >
                댓글단글
              </Button>
            </ButtonGroup>

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
                  <TableCell align='center'>내용</TableCell>
                  <TableCell align='center'>작성자</TableCell>
                  <TableCell align='center'>좋아요</TableCell>
                  <TableCell align='center'>댓글</TableCell>
                  <TableCell align='center'>조회수</TableCell>
                  <TableCell align='center'></TableCell>
                  <TableCell align='center'></TableCell>
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
                      if (postId !== row.id) setPostId(row.id);
                      else reloadPost();
                    }}
                  >
                    <TableCell align='center'>{row.id}</TableCell>
                    <TableCell align='center'>{row.content}</TableCell>
                    <TableCell align='center'>{row.isUserWithdraw ? '탈퇴한유저' : row.anonymity ? '익명' : row.user.nickname}</TableCell>
                    <TableCell align='center'>{row.likes}</TableCell>
                    <TableCell align='center'>{row.countOfComment}</TableCell>
                    <TableCell align='center'>{row.hits}</TableCell>
                    <TableCell align='center'>
                      <IconButton
                        onClick={(e) => {
                          e.stopPropagation();
                          row.isLiked
                            ? axiosInstance
                                .delete(`post/${row.id}/like`)
                                .then((res) => {
                                  fetchData();
                                })
                                .catch((res) => {
                                  alert(res.response.data.message);
                                })
                            : axiosInstance
                                .post(`post/${row.id}/like`)
                                .then((res) => {
                                  fetchData();
                                })
                                .catch((res) => {
                                  alert(res.response.data.message);
                                });
                        }}
                      >
                        {row.isLiked ? <FavoriteIcon /> : <FavoriteBorderIcon />}
                      </IconButton>
                    </TableCell>
                    <TableCell align='center'>
                      <IconButton
                        onClick={(e) => {
                          e.stopPropagation();
                          row.isBookmarked
                            ? axiosInstance
                                .delete(`post/${row.id}/bookmark`)
                                .then((res) => {
                                  fetchData();
                                })
                                .catch((res) => {
                                  alert(res.response.data.message);
                                })
                            : axiosInstance
                                .post(`post/${row.id}/bookmark`)
                                .then((res) => {
                                  fetchData();
                                })
                                .catch((res) => {
                                  alert(res.response.data.message);
                                });
                        }}
                      >
                        {row.isBookmarked ? <BookmarkIcon /> : <BookmarkBorderIcon />}
                      </IconButton>
                    </TableCell>
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
                                      reloadPost();
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
                                reloadPost();
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

                  <div style={{ display: 'flex', flexDirection: 'column', gap: 20, marginTop: '20px' }}>
                    {comments.length
                      ? comments.map((comment, idx) => (
                          <Comment comment={comment} reloadPost={reloadPost} fetchData={fetchData} post={post}></Comment>
                        ))
                      : ''}
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

const Comment = ({ comment, reloadPost, fetchData, post, isChild }) => {
  const [commentContent, setCommentContent] = useState('');
  const [commentAnnonimity, setCommentAnnonimity] = useState(false);
  const [modifyMode, setModifyMode] = useState(false);
  const [modifyContent, setModifyContent] = useState(comment.content ? comment.content : '');

  const [childs, setChilds] = useState([]);

  useEffect(() => {
    comment.childs && setChilds([...comment.childs]);
  }, [comment]);

  return (
    <div style={{ paddingLeft: isChild ? '30px' : '10px' }}>
      <div style={{ display: 'flex', gap: 10, alignItems: 'center' }}>
        <Avatar src={comment.user?.profileImg} sx={{ width: 26, height: 26 }} />
        <div>
          <div style={{ fontWeight: '400', fontSize: '15px' }}>
            {!comment.status
              ? '삭제된 댓글'
              : comment.isUserWithdraw
              ? '탈퇴한유저'
              : comment.anonymity
              ? '익명' + (comment.anonymityId ? comment.anonymityId : '')
              : comment.user?.nickname}{' '}
            {comment.isWriter && '(글작성자)'}
          </div>
          <div style={{ fontWeight: '200', fontSize: '10px', marginTop: '-3px' }}>
            {comment.createdAt} {comment.isModified && '(수정됨)'}
          </div>
        </div>
        <div>
          {comment.isMine && (
            <ButtonGroup size={'small'}>
              <Button
                sx={{ marginBottom: 0.5 }}
                onClick={(e) => {
                  e.stopPropagation();
                  setModifyMode(!modifyMode);
                }}
              >
                {!modifyMode ? '수정' : '수정취소'}
              </Button>
              <Button
                sx={{ marginBottom: 0.5 }}
                onClick={(e) => {
                  e.stopPropagation();
                  axiosInstance.delete(`comment/${comment.id}`).then((res) => {
                    setModifyMode(false);
                    fetchData();
                    reloadPost();
                  });
                }}
              >
                삭제
              </Button>
            </ButtonGroup>
          )}
        </div>
      </div>

      <div style={{ fontWeight: '500', fontSize: '17px', width: '100%', paddingLeft: '15px' }}>
        {!modifyMode ? (
          comment.content
        ) : (
          <>
            <TextField
              size='small'
              label='수정할 내용'
              id='fullWidth'
              value={modifyContent}
              onChange={(e) => {
                setModifyContent(e.target.value);
              }}
            />
            <Button
              size='sx'
              variant='contained'
              onClick={() => {
                axiosInstance
                  .put(`comment/${comment.id}`, { content: modifyContent })
                  .then((res) => {
                    setModifyMode(false);
                    fetchData();
                    reloadPost();
                  })
                  .catch((res) => {
                    alert(res.response.data.message);
                  });
              }}
            >
              수정
            </Button>
          </>
        )}
      </div>
      <div style={{ display: 'flex', flexDirection: 'column', gap: '6px' }}>
        {childs && childs.map((childComments, idx) => <Comment comment={childComments} isChild={true}></Comment>)}
      </div>
      {!isChild && comment.status && (
        <div style={{ paddingLeft: '15px', marginTop: '5px' }}>
          <TextField
            size='small'
            label='대댓글'
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
                .post(`post/${post.id}/comment`, { content: commentContent, anonymity: commentAnnonimity, parent: { id: comment.id } })
                .then((res) => {
                  reloadPost();
                  fetchData();
                  setCommentAnnonimity(false);
                  setCommentContent('');
                })
                .catch((res) => {
                  alert(res.response.data.message);
                });
            }}
          >
            입력
          </Button>
          <Checkbox
            checked={commentAnnonimity}
            onChange={(e) => {
              setCommentAnnonimity(e.target.checked);
            }}
          ></Checkbox>
          익명
        </div>
      )}
    </div>
  );
};

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
