/**
 * This program accesses an already made React component
 * to accomplish file upload from the computer.
 * Only audio files are allowed to be uploaded
 */

import React from 'react';
import { useFileUpload } from 'use-file-upload';
import cogoToast from 'cogo-toast';
import axios from 'axios';

const FileUpload = props => {
  const [file, selectFile] = useFileUpload()

  return (
    <div>
      <button className='btn'
        onClick={() => {
          // Single File Upload
          selectFile({ accept: 'audio/*' }, ({ source, name, size, file }) => {
            // file - is the raw File Object
            // Todo: Upload to cloud.
		console.log(props.token.token);
//excuse the mess here, this is currently being worked on.
		/**	axios.get("https://alarmbuddy.wm.r.appspot.com/download/johnny/erokia.wav", {
			          headers: { authorization: props.token.token },
			          //params: {
			          //}
		          }
		          //{ withCredentials: true }
              ).then(response => {
			          console.log("sound upload res: ", response);
			          if(response.data.status === 200) {
				          cogoToast.success("Audio successfully uploaded!", {position: 'top-right'});
			          }
		          }).catch(error => {
			          console.log("Sound upload error: ", error);
		          });
*/
		//file.mimetype = "application/octet-stream";
		console.log(file);
            cogoToast.loading('Uploading file', {position: 'top-right'}).then(() => {
              axios.post("https://alarmbuddy.wm.r.appspot.com/upload/shoob", {
			          headers: { authorization: props.token.token },
			          //params: {
			          file: file
			          //}
		          }
		          //{ withCredentials: true }
              ).then(response => {
			          console.log("sound upload res: ", response);
			          if(response.data.status === 200) {
				          cogoToast.success("Audio successfully uploaded!", {position: 'top-right'});
			          }
		          }).catch(error => {
			          console.log("Sound upload error: ", error);
		          });
            });
          })
        }}
      >
        Click to Upload a file
      </button>

      {file ? 
        (<div>
          <img src={file.source} alt='preview' />
          <span> Name: {file.name} </span>
          <span> Size: {file.size} </span>
        </div>) : 
        (<span>No file selected</span>
      )}
    </div>
  )
}

export default FileUpload