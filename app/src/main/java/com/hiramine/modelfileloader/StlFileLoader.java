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
package com.hiramine.modelfileloader;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.StringTokenizer;

import android.util.Log;

public class StlFileLoader
{
	public static MFLModel load( String strPath, OnProgressListener onProgressListener )
	{
		File file      = new File( strPath );
		if( 0 == file.length() )
		{
			return null;
		}

		// ファーストパース（要素数カウント）
		int[] aiCountTriangle = new int[1];
		int[] aiCountLine     = new int[1];
		if( !parse_first( strPath, aiCountTriangle, aiCountLine ) )
		{
			return null;
		}

		// 領域確保
		if( 0 == aiCountTriangle[0] )
		{
			return null;
		}
		float[] af3Vertex = new float[aiCountTriangle[0] * 3 * 3]; // af3Vertexは、3つのデータで一つの頂点、さらにそれが3つで一つの三角形

		// セカンドパース（値詰め）
		if( !parse_second( strPath, af3Vertex, aiCountLine[0], onProgressListener ) )
		{
			return null;
		}

		// 領域確保、データ構築
		MFLModel model = new MFLModel();
		model.iCountVertex = af3Vertex.length / 3;
		model.af3Vertex = af3Vertex;
		model.iCountTriangle = model.iCountVertex / 3;
		model.aIndexedTriangle = new MFLIndexedTriangle[model.iCountTriangle];
		for( int iIndexTriangle = 0; iIndexTriangle < model.iCountTriangle; ++iIndexTriangle )
		{
			model.aIndexedTriangle[iIndexTriangle] = new MFLIndexedTriangle();
			model.aIndexedTriangle[iIndexTriangle].i3IndexVertex[0] = (short)( iIndexTriangle * 3 + 0 );
			model.aIndexedTriangle[iIndexTriangle].i3IndexVertex[1] = (short)( iIndexTriangle * 3 + 1 );
			model.aIndexedTriangle[iIndexTriangle].i3IndexVertex[2] = (short)( iIndexTriangle * 3 + 2 );
		}
		model.iCountNormal = 0;
		model.af3Normal = null;
		model.aMaterial = null;
		model.groupRoot = new MFLGroup( "" );
		model.groupRoot.iCountTriangle = model.iCountTriangle;
		model.groupRoot.aiIndexTriangle = new int[model.groupRoot.iCountTriangle];
		for( int iIndexTriangle = 0; iIndexTriangle < model.groupRoot.iCountTriangle; ++iIndexTriangle )
		{
			model.groupRoot.aiIndexTriangle[iIndexTriangle] = iIndexTriangle;
		}
		return model;
	}

	private static boolean parse_first( String strPath, int[] aiCountTriangle, int[] aiCountLine )
	{
		// インプットのチェック
		if( null == aiCountTriangle
			|| null == aiCountLine )
		{
			return false;
		}
		// アウトプットの初期化
		aiCountTriangle[0] = 0;
		aiCountLine[0] = 0;

		try
		{
			// 読み取り
			BufferedReader br = new BufferedReader( new FileReader( strPath ) );

			int iIndexTriangle = 0;
			int iIndexLine     = 0;
			while( true )
			{
				++iIndexLine;
				String strReadString = br.readLine();
				if( null == strReadString )
				{
					break;
				}
				StringTokenizer stReadString = new StringTokenizer( strReadString, ", \t\r\n" );
				if( !stReadString.hasMoreTokens() )
				{
					continue;
				}
				String token = stReadString.nextToken();
				if( token.equalsIgnoreCase( "endfacet" ) )
				{
					++iIndexTriangle;
					continue;
				}
			}

			br.close();

			aiCountTriangle[0] = iIndexTriangle;
			aiCountLine[0] = iIndexLine;
			return true;
		}
		catch( Exception e )
		{
			Log.e( "StlFileLoader", "parse_first error : " + e );
			return false;
		}
	}

	private static boolean parse_second( String strPath, float[] af3Vertex, int iCountLine, OnProgressListener onProgressListener )
	{
		// インプットのチェック
		if( null == af3Vertex )
		{
			return false;
		}

		try
		{
			// 読み取り
			BufferedReader br = new BufferedReader( new FileReader( strPath ) );

			int iIndexTriangle = 0;
			int iIndexLine     = 0;
			int iIndex3        = 0;
			while( true )
			{
				if( null != onProgressListener
					&& 0 == iIndexLine % 100 )
				{
					if( !onProgressListener.updateProgress( iIndexLine, iCountLine ) )
					{    // ユーザー操作による処理中止
						Log.d( "LoaderStlFile", "Cancelled" );
						return false;
					}
				}
				++iIndexLine;
				String strReadString = br.readLine();
				if( null == strReadString )
				{
					break;
				}
				StringTokenizer stReadString = new StringTokenizer( strReadString, ", \t\r\n" );
				if( !stReadString.hasMoreTokens() )
				{
					continue;
				}
				String token = stReadString.nextToken();
				if( token.equalsIgnoreCase( "vertex" ) )
				{
					if( 3 <= iIndex3 )
					{
						continue;
					}
					af3Vertex[iIndexTriangle * 9 + iIndex3 * 3 + 0] = Float.valueOf( stReadString.nextToken() );
					af3Vertex[iIndexTriangle * 9 + iIndex3 * 3 + 1] = Float.valueOf( stReadString.nextToken() );
					af3Vertex[iIndexTriangle * 9 + iIndex3 * 3 + 2] = Float.valueOf( stReadString.nextToken() );
					++iIndex3;
					continue;
				}
				else if( token.equalsIgnoreCase( "facet" ) )
				{ // 面法線ベクトル
					iIndex3 = 0;
					continue;
				}
				else if( token.equalsIgnoreCase( "endfacet" ) )
				{
					++iIndexTriangle;
					continue;
				}
				else if( token.equalsIgnoreCase( "solid" ) )
				{ // ソリッド名
					continue;
				}
			}

			br.close();

			return true;
		}
		catch( Exception e )
		{
			Log.e( "StlFileLoader", "parse_second error : " + e );
			return false;
		}
	}
}
