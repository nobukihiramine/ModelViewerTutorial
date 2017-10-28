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

import android.opengl.GLSurfaceView;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

public class MainActivity extends AppCompatActivity
{
	// メンバー変数
	private ModelViewerView m_modelviewerview;

	@Override
	protected void onCreate( Bundle savedInstanceState )
	{
		super.onCreate( savedInstanceState );
		setContentView( R.layout.activity_main );

		// GLSurfaceViewの取得
		m_modelviewerview = (ModelViewerView)findViewById( R.id.glview );
	}

	// 初回表示時、および、ポーズからの復帰時
	@Override
	protected void onResume()
	{
		super.onResume();

		m_modelviewerview.onResume();
	}

	// 別のアクティビティ（か別のアプリ）に移行したことで、バックグラウンドに追いやられた時
	@Override
	protected void onPause()
	{
		m_modelviewerview.onPause();

		super.onPause();
	}

	// オプションメニュー作成時の処理
	@Override
	public boolean onCreateOptionsMenu( Menu menu )
	{
		getMenuInflater().inflate( R.menu.activity_main, menu );
		switch( m_modelviewerview.m_eTrackingMode_1fingerdrag )
		{
			case TM_NONE:
				menu.findItem( R.id.menuitem_drag_none ).setChecked( true );
				break;
			case TM_ROTATE:
				menu.findItem( R.id.menuitem_drag_rotate ).setChecked( true );
				break;
			case TM_PAN:
				menu.findItem( R.id.menuitem_drag_pan ).setChecked( true );
				break;
			case TM_ZOOM:
				menu.findItem( R.id.menuitem_drag_zoom ).setChecked( true );
				break;
		}
		menu.findItem( R.id.menuitem_render_point ).setChecked( m_modelviewerview.getRenderer().m_bRenderPoint );
		menu.findItem( R.id.menuitem_render_line ).setChecked( m_modelviewerview.getRenderer().m_bRenderLine );
		menu.findItem( R.id.menuitem_render_face ).setChecked( m_modelviewerview.getRenderer().m_bRenderFace );
		menu.findItem( R.id.menuitem_pick_point ).setChecked( m_modelviewerview.getRenderer().m_bPickPoint );
		menu.findItem( R.id.menuitem_pick_line ).setChecked( m_modelviewerview.getRenderer().m_bPickLine );
		menu.findItem( R.id.menuitem_pick_face ).setChecked( m_modelviewerview.getRenderer().m_bPickFace );
		return true;
	}

	// オプションメニューのアイテム選択時の処理
	@Override
	public boolean onOptionsItemSelected( MenuItem item )
	{
		switch( item.getItemId() )
		{
			case R.id.menuitem_drag_none:
				item.setChecked( true );
				m_modelviewerview.m_eTrackingMode_1fingerdrag = OpenGLTrackRenderer.ETrackingMode.TM_NONE;
				return true;
			case R.id.menuitem_drag_rotate:
				item.setChecked( true );
				m_modelviewerview.m_eTrackingMode_1fingerdrag = OpenGLTrackRenderer.ETrackingMode.TM_ROTATE;
				return true;
			case R.id.menuitem_drag_pan:
				item.setChecked( true );
				m_modelviewerview.m_eTrackingMode_1fingerdrag = OpenGLTrackRenderer.ETrackingMode.TM_PAN;
				return true;
			case R.id.menuitem_drag_zoom:
				item.setChecked( true );
				m_modelviewerview.m_eTrackingMode_1fingerdrag = OpenGLTrackRenderer.ETrackingMode.TM_ZOOM;
				return true;
			case R.id.menuitem_render_point:
				item.setChecked( !item.isChecked() );
				m_modelviewerview.getRenderer().m_bRenderPoint = item.isChecked();
				m_modelviewerview.requestRender(); // 再描画
				return true;
			case R.id.menuitem_render_line:
				item.setChecked( !item.isChecked() );
				m_modelviewerview.getRenderer().m_bRenderLine = item.isChecked();
				m_modelviewerview.requestRender(); // 再描画
				return true;
			case R.id.menuitem_render_face:
				item.setChecked( !item.isChecked() );
				m_modelviewerview.getRenderer().m_bRenderFace = item.isChecked();
				m_modelviewerview.requestRender(); // 再描画
				return true;
			case R.id.menuitem_pick_point:
				item.setChecked( !item.isChecked() );
				m_modelviewerview.getRenderer().m_bPickPoint = item.isChecked();
				return true;
			case R.id.menuitem_pick_line:
				item.setChecked( !item.isChecked() );
				m_modelviewerview.getRenderer().m_bPickLine = item.isChecked();
				return true;
			case R.id.menuitem_pick_face:
				item.setChecked( !item.isChecked() );
				m_modelviewerview.getRenderer().m_bPickFace = item.isChecked();
				return true;
		}
		return true;
	}
}
