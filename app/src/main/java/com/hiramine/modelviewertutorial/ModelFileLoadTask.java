/*
 * Copyright 2017 Nobuki HIRAMINE
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.hiramine.modelviewertutorial;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.AsyncTask;

import com.hiramine.modelfileloader.OnProgressListener;

public class ModelFileLoadTask extends AsyncTask<Void, Integer, Model> implements DialogInterface.OnCancelListener, DialogInterface.OnClickListener, OnProgressListener
{
	// メンバー変数
	private ProgressDialog  m_progressdialog;
	private ModelViewerView m_modelviewerview;
	private String          m_strPath;

	// コンストラクタ
	public ModelFileLoadTask( Context context, ModelViewerView modelviewerview, String strPath )
	{
		m_progressdialog = new ProgressDialog( context );
		m_modelviewerview = modelviewerview;
		m_strPath = strPath;
	}

	// タスクの前処理
	@Override
	protected void onPreExecute()
	{
		// プログレスダイアログを表示
		m_progressdialog.setProgressStyle( ProgressDialog.STYLE_HORIZONTAL );
		m_progressdialog.setMax( 100 );
		m_progressdialog.setTitle( "Load model file" );
		m_progressdialog.setMessage( m_strPath );
		m_progressdialog.setCancelable( true );
		m_progressdialog.setOnCancelListener( this );
		m_progressdialog.setButton( DialogInterface.BUTTON_NEGATIVE, "Cancel", this );
		m_progressdialog.show();
	}

	// タスク
	@Override
	protected Model doInBackground( Void... params )
	{
		updateProgress( 0, 100 );    // タスク実行開始後に直ぐにプログレスダイアログを表示するための進捗更新（onProgress→publishProgress→onProgressUpdate）

		return ModelFileLoader.load( m_strPath, this );
	}

	// 進捗の更新
	@Override
	protected void onProgressUpdate( Integer... progress )
	{
		m_progressdialog.setMax( progress[1] );
		m_progressdialog.setProgress( progress[0] );
	}

	// タスクの後処理
	@Override
	protected void onPostExecute( Model result )
	{
		m_modelviewerview.setModel( result );
		// プログレスダイアログを閉じる
		m_progressdialog.dismiss();
	}

	// タスクがキャンセルされたときの処理
	@Override
	protected void onCancelled()
	{
		// プログレスダイアログを閉じる
		m_progressdialog.dismiss();
		m_progressdialog = null;
	}

	// 進捗ダイアログがキャンセルされた
	public void onCancel( DialogInterface arg0 )
	{
		this.cancel( true ); // タスクのキャンセル要請
	}

	// 「キャンセルボタン」ボタンが押された
	public void onClick( DialogInterface arg0, int arg1 )
	{
		m_progressdialog.cancel(); // 進捗ダイアログのキャンセル要請
	}

	// 進捗の監視対象の進捗が更新された
	public boolean updateProgress( int iPos, int iMax )
	{
		publishProgress( iPos, iMax ); // onProgressUpdateに情報をトス
		return !isCancelled();    // 処理続行か否か（処理続行:true, 処理中止:false）
	}
}
