# API Summary

## 1. 파일 관리
```
1. POST /files : 새 파일을 업로드합니다.
2. GET /files/{fileId} : 특정 파일의 정보를 조회합니다.
3. PUT /files/{fileId} : 특정 파일의 정보를 업데이트합니다 (예: 이름 변경, 폴더 이동).
4. DELETE /files/{fileId} : 특정 파일을 삭제합니다.
5. GET /files/{fileID}/download : 특정 파일을 다운로드 합니다.
```

## 2. 폴더 관리
```
1. POST /folders : 새 폴더를 생성합니다.
2. GET /folders/{folderId} : 특정 폴더의 정보 및 폴더 내 파일을 조회합니다.
3. PUT /folders/{folderId} : 특정 폴더의 정보를 업데이트합니다 (예: 이름 변경, 폴더 이동).
4. DELETE /folders/{folderId} : 특정 폴더를 삭제합니다.
```