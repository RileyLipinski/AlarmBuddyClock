import React from 'react';
import { useFileUpload } from 'use-file-upload';
import cogoToast from 'cogo-toast';

const FileUpload = () => {
  const [file, selectFile] = useFileUpload()

  return (
    <div>
      <button className='btn'
        onClick={() => {
          // Single File Upload
          selectFile({ accept: 'audio/*' }, ({ source, name, size, file1 }) => {
            // file - is the raw File Object
            console.log({ source, name, size, file1 })
            // Todo: Upload to cloud.
            cogoToast.loading('Uploading file', {position: 'top-right'}).then(() => {
              cogoToast.success("File successfully uploaded!", {position: 'top-right'});
            });
          })
        }}
      >
        Click to Upload a file
      </button>

      {file ? (
        <div>
          <img src={file.source} alt='preview' />
          <span> Name: {file.name} </span>
          <span> Size: {file.size} </span>
        </div>
      ) : (
        <span>No file selected</span>
      )}
    </div>
  )
}
//URGENT: we need to get rid of this ternary operator file ? (blah) : (blah)
export default FileUpload