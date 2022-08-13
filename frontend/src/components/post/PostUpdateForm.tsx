import { ChangeEvent, MouseEvent, useRef, useState } from "react";
import {
  PostUpdateSend,
  setPostUpdateFormState,
  setModalPostState,
} from "../../store/postModal";
import { postModify, PostData } from "../../store/post";
import { useDispatch, useSelector } from "react-redux";
import { postUpdate } from "../../lib/withTokenApi";
import { RootState } from "../../store/store";

import EditorComponent from "../ui/Editor";
import ReactQuill from "react-quill";

import styles from "./PostUpdateForm.module.scss";
import { storageService } from "../../fbase/fbase";
import { getDownloadURL, ref, uploadBytes } from "firebase/storage";
// 생성폼 -> <PostUpdateForm post = {null}/>
// 수정폼 -> <PostForm post = {수정할려는 포스트 데이터}/>

const PostUpdateForm: React.FC<{}> = () => {
  const { postModalState: PostModalState } = useSelector(
    (state: RootState) => state.postModal
  );
  console.log(PostModalState);

  const dispatch = useDispatch();

  const titleInputRef = useRef<HTMLInputElement>(null);
  const contentInputRef = useRef<ReactQuill>();

  const [file, setFile] = useState<File>();
  const [previewImage, setPreviewImage] = useState("");
  const [fileName, setFileName] = useState("");

  const postingUpdateHandler = (event: MouseEvent) => {
    event.preventDefault();
    console.log("!!!", contentInputRef.current!.value);
    const PostUpdateData: PostUpdateSend = {
      postId: PostModalState!.id!,
      title: titleInputRef.current!.value,
      content: contentInputRef.current!.value,
      postImg: PostModalState!.postImg,
    };

    const modifiedModalPost: PostData = {
      id: PostModalState!.id!,
      title: titleInputRef.current!.value,
      content: contentInputRef.current!.value,
      postImg: PostModalState!.postImg,
      createdTime: PostModalState!.createdTime,
      modifiedTime: PostModalState!.modifiedTime,
      writer: PostModalState!.writer,
      liked: PostModalState!.liked,
      likeNum: PostModalState!.likeNum,
    };

    postUpdate(PostUpdateData)
      .then((res) => {
        // 이미지 업로드
        if (file) {
          const imgRef = ref(storageService, `post/${PostUpdateData.postId}`);
          uploadBytes(imgRef, file)
            .then((res) => {
              getDownloadURL(res.ref)
                .then((res) => {
                  PostUpdateData.postImg = res;
                  modifiedModalPost.postImg = res;
                  postUpdate(PostUpdateData)
                    .then((res) => {
                      console.log("post 수정완료", res);
                      dispatch(postModify(modifiedModalPost));
                      dispatch(setPostUpdateFormState(false));
                      dispatch(setModalPostState(modifiedModalPost));
                    })
                    .catch((err) => console.log("이미지 db에 저장 실패", err));
                })
                .catch((err) => console.log("이미지 url 가져오기 실패", err));
              setPreviewImage("");
            })
            .catch((err) => console.log("이미지 firestore에 업로드 실패", err));
        } else {
          console.log("post 수정완료", res);
          dispatch(postModify(modifiedModalPost));
          dispatch(setPostUpdateFormState(false));
          dispatch(setModalPostState(modifiedModalPost));
        }
      })
      .catch((err) => {
        console.log("err", err);
      });
  };

  // 이미지 로드
  const onLoadHandler = (event: ChangeEvent<HTMLInputElement>) => {
    event.preventDefault();
    const fileList = event.target.files;

    if (fileList) {
      // console.log(fileList[0].name);
      setFileName(fileList[0].name);
      setFile(fileList[0]);
      setPreviewImage(URL.createObjectURL(fileList[0]));
    }
  };

  return (
    <div>
      <form className={styles.postUpdateForm}>
        <div>
          <label htmlFor="title">제목</label>
          <input
            type="text"
            id="title"
            ref={titleInputRef}
            defaultValue={PostModalState!.title!}
          />
        </div>
        <div>
          <input
            value={fileName ? fileName : "첨부파일"}
            placeholder="첨부파일"
          />
          <label htmlFor="img">파일 찾기</label>
          <input
            type="file"
            accept="image/*"
            id="img"
            onChange={onLoadHandler}
          />
        </div>
        {previewImage && <img src={previewImage} alt="img" />}
        <div>
          <EditorComponent
            QuillRef={contentInputRef}
            value={PostModalState!.content!}
          />
        </div>
        <button onClick={postingUpdateHandler}>등록하기</button>
      </form>
    </div>
  );
};
export default PostUpdateForm;
