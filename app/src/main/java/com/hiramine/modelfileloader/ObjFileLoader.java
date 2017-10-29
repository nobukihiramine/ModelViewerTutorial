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

public class ObjFileLoader
{
	// 定数
	private static final String NONAME = "(NoName)";

	public static MFLModel load( String strPath, OnProgressListener onProgressListener )
	{
		File file = new File( strPath );
		if( 0 == file.length() )
		{
			return null;
		}

		MFLModel model = new MFLModel();

		// ファーストパース（要素数カウント）
		int[] aiCountLine = new int[1];
		if( !parse_first( strPath, model, file.getParent(), aiCountLine ) )
		{
			return null;
		}

		// 領域確保
		if( 0 == model.iCountVertex
			|| 0 == model.iCountTriangle )
		{
			return null;
		}
		model.af3Vertex = new float[3 * model.iCountVertex];
		model.aIndexedTriangle = new MFLIndexedTriangle[model.iCountTriangle];
		for( int iIndexTriangle = 0; iIndexTriangle < model.aIndexedTriangle.length; ++iIndexTriangle )
		{
			model.aIndexedTriangle[iIndexTriangle] = new MFLIndexedTriangle();
		}
		if( 0 != model.iCountNormal )
		{
			model.af3Normal = new float[3 * model.iCountNormal];
		}
		MFLGroup group_current = model.groupRoot;
		while( null != group_current )
		{
			if( 0 != group_current.iCountTriangle )
			{
				group_current.aiIndexTriangle = new int[group_current.iCountTriangle];
			}
			group_current = group_current.groupNext;
		}

		// セカンドパース（値詰め）
		if( !parse_second( strPath, model, aiCountLine[0], onProgressListener ) )
		{
			return null;
		}

		return model;
	}

	private static boolean parse_first( String strPath, MFLModel model, String strModelFileDir, int[] aiCountLine )
	{
		// インプットのチェック
		if( null == model
			|| null == aiCountLine )
		{
			return false;
		}
		// アウトプットの初期化
		aiCountLine[0] = 0;

		// カウンターのリセット
		model.iCountVertex = 0;
		model.iCountNormal = 0;
		model.iCountTriangle = 0;
		MFLGroup group_current = model.groupRoot;
		while( null != group_current )
		{
			group_current.iCountTriangle = 0;
			group_current = group_current.groupNext;
		}

		try
		{
			// 読み取り
			BufferedReader br = new BufferedReader( new FileReader( strPath ) );

			group_current = model.addGroup( NONAME );
			if( null == group_current )
			{
				return false;
			}
			int iIndexLine = 0;
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
				switch( token.charAt( 0 ) )
				{
					case '#': // comment
						break;
					case 'v': // v, vn, vt
						if( 1 == token.length() )
						{ //vertex
							++( model.iCountVertex );
						}
						else if( 'n' == token.charAt( 1 ) )
						{ // normal
							++( model.iCountNormal );
						}
						else if( 't' == token.charAt( 1 ) )
						{ // texcoord
							break;
						}
						break;
					case 'm': // matlib
						if( stReadString.hasMoreTokens() )
						{
							token = stReadString.nextToken();
							model.aMaterial = MtlFileLoader.load( strModelFileDir + "/" + token );
						}
						break;
					case 'u': // usermtl
						break;
					case 'g': // group
						if( stReadString.hasMoreTokens() )
						{
							group_current = model.addGroup( stReadString.nextToken() );
						}
						else
						{
							group_current = model.addGroup( NONAME );
						}
						break;
					case 'f': // face
						if( stReadString.hasMoreTokens() )
						{
							stReadString.nextToken(); // 1点目
							stReadString.nextToken(); // 2点目
							stReadString.nextToken(); // 3点目
							++( model.iCountTriangle ); // ここまでで1つの三角形、以降１つ点が増えるごとに、三角形が１つ増える
							++( group_current.iCountTriangle );
							while( stReadString.hasMoreTokens() )
							{ // 四角形以上対応
								++( model.iCountTriangle );
								++( group_current.iCountTriangle );
								stReadString.nextToken();
							}
						}
				}
			}

			br.close();

			aiCountLine[0] = iIndexLine;
			return true;
		}
		catch( Exception e )
		{
			Log.e( "ObjFileLoader", "parse_first error : " + e );
			return false;
		}
	}

	private static boolean parse_second( String strPath, MFLModel model, int iCountLine, OnProgressListener onProgressListener )
	{
		// インプットのチェック
		if( null == model )
		{
			return false;
		}

		// カウンターのリセット
		model.iCountVertex = 0;
		model.iCountNormal = 0;
		model.iCountTriangle = 0;
		MFLGroup group_current = model.groupRoot;
		while( null != group_current )
		{
			group_current.iCountTriangle = 0;
			group_current = group_current.groupNext;
		}

		try
		{
			// 読み取り
			BufferedReader br = new BufferedReader( new FileReader( strPath ) );

			float f0;
			float f1;
			float f2;
			int   iIndexMaterial_current = 0;
			group_current = model.findGroup( NONAME );
			int iIndexLine = 0;
			while( true )
			{
				if( null != onProgressListener
					&& 0 == iIndexLine % 100 )
				{
					if( !onProgressListener.updateProgress( iIndexLine, iCountLine ) )
					{    // ユーザー操作による処理中止
						Log.d( "ObjFileLoader", "Cancelled" );
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
				switch( token.charAt( 0 ) )
				{
					case '#': // comment
						break;
					case 'v': // v, vn, vt
						f0 = 0.0f;
						f1 = 0.0f;
						f2 = 0.0f;
						if( stReadString.hasMoreTokens() )
						{
							f0 = Float.parseFloat( stReadString.nextToken() );
						}
						if( stReadString.hasMoreTokens() )
						{
							f1 = Float.parseFloat( stReadString.nextToken() );
						}
						if( stReadString.hasMoreTokens() )
						{
							f2 = Float.parseFloat( stReadString.nextToken() );
						}
						if( 1 == token.length() )
						{ // vertex
							model.af3Vertex[3 * model.iCountVertex + 0] = f0;
							model.af3Vertex[3 * model.iCountVertex + 1] = f1;
							model.af3Vertex[3 * model.iCountVertex + 2] = f2;
							++( model.iCountVertex );
						}
						else if( 'n' == token.charAt( 1 ) )
						{ // normal
							model.af3Normal[3 * model.iCountNormal + 0] = f0;
							model.af3Normal[3 * model.iCountNormal + 1] = f1;
							model.af3Normal[3 * model.iCountNormal + 2] = f2;
							++( model.iCountNormal );
						}
						else if( 't' == token.charAt( 1 ) )
						{ // texcoord
						}
						break;
					case 'm': // matlib
						break;
					case 'u': // usermtl
						if( stReadString.hasMoreTokens() )
						{
							group_current.iIndexMaterial = iIndexMaterial_current = model.findMaterial( stReadString.nextToken() );
						}
						else
						{
							group_current.iIndexMaterial = iIndexMaterial_current = model.findMaterial( "" );
						}
						break;
					case 'g': // group
						if( stReadString.hasMoreTokens() )
						{
							group_current = model.addGroup( stReadString.nextToken() );
						}
						else
						{
							group_current = model.addGroup( NONAME );
						}
						group_current.iIndexMaterial = iIndexMaterial_current;
						break;
					case 'f': // face
						if( stReadString.hasMoreTokens() )
						{
							token = stReadString.nextToken();
							if( token.matches( "[0-9]+//[0-9]+" ) )
							{ // v//n
								StringTokenizer stVTN = new StringTokenizer( token, "/" );
								model.aIndexedTriangle[model.iCountTriangle].i3IndexVertex[0] = (short)( Short.parseShort( stVTN.nextToken() ) - 1 );
								model.aIndexedTriangle[model.iCountTriangle].i3IndexNormal[0] = (short)( Short.parseShort( stVTN.nextToken() ) - 1 );
								stVTN = new StringTokenizer( stReadString.nextToken(), "/" );
								model.aIndexedTriangle[model.iCountTriangle].i3IndexVertex[1] = (short)( Short.parseShort( stVTN.nextToken() ) - 1 );
								model.aIndexedTriangle[model.iCountTriangle].i3IndexNormal[1] = (short)( Short.parseShort( stVTN.nextToken() ) - 1 );
								stVTN = new StringTokenizer( stReadString.nextToken(), "/" );
								model.aIndexedTriangle[model.iCountTriangle].i3IndexVertex[2] = (short)( Short.parseShort( stVTN.nextToken() ) - 1 );
								model.aIndexedTriangle[model.iCountTriangle].i3IndexNormal[2] = (short)( Short.parseShort( stVTN.nextToken() ) - 1 );
								group_current.aiIndexTriangle[group_current.iCountTriangle] = model.iCountTriangle;
								++( model.iCountTriangle );
								++( group_current.iCountTriangle );
								while( stReadString.hasMoreTokens() )
								{ // 四角形以上対応
									stVTN = new StringTokenizer( stReadString.nextToken(), "/" );
									model.aIndexedTriangle[model.iCountTriangle].i3IndexVertex[0] = model.aIndexedTriangle[model.iCountTriangle - 1].i3IndexVertex[0];
									model.aIndexedTriangle[model.iCountTriangle].i3IndexNormal[0] = model.aIndexedTriangle[model.iCountTriangle - 1].i3IndexNormal[0];
									model.aIndexedTriangle[model.iCountTriangle].i3IndexVertex[1] = model.aIndexedTriangle[model.iCountTriangle - 1].i3IndexVertex[2];
									model.aIndexedTriangle[model.iCountTriangle].i3IndexNormal[1] = model.aIndexedTriangle[model.iCountTriangle - 1].i3IndexNormal[2];
									model.aIndexedTriangle[model.iCountTriangle].i3IndexVertex[2] = (short)( Short.parseShort( stVTN.nextToken() ) - 1 );
									model.aIndexedTriangle[model.iCountTriangle].i3IndexNormal[2] = (short)( Short.parseShort( stVTN.nextToken() ) - 1 );
									group_current.aiIndexTriangle[group_current.iCountTriangle] = model.iCountTriangle;
									++( model.iCountTriangle );
									++( group_current.iCountTriangle );
								}
							}
							else if( token.matches( "[0-9]+/[0-9]+/[0-9]+" ) )
							{ // v/t/n
								StringTokenizer stVTN = new StringTokenizer( token, "/" );
								model.aIndexedTriangle[model.iCountTriangle].i3IndexVertex[0] = (short)( Short.parseShort( stVTN.nextToken() ) - 1 );
								stVTN.nextToken();
								model.aIndexedTriangle[model.iCountTriangle].i3IndexNormal[0] = (short)( Short.parseShort( stVTN.nextToken() ) - 1 );
								stVTN = new StringTokenizer( stReadString.nextToken(), "/" );
								model.aIndexedTriangle[model.iCountTriangle].i3IndexVertex[1] = (short)( Short.parseShort( stVTN.nextToken() ) - 1 );
								stVTN.nextToken();
								model.aIndexedTriangle[model.iCountTriangle].i3IndexNormal[1] = (short)( Short.parseShort( stVTN.nextToken() ) - 1 );
								stVTN = new StringTokenizer( stReadString.nextToken(), "/" );
								model.aIndexedTriangle[model.iCountTriangle].i3IndexVertex[2] = (short)( Short.parseShort( stVTN.nextToken() ) - 1 );
								stVTN.nextToken();
								model.aIndexedTriangle[model.iCountTriangle].i3IndexNormal[2] = (short)( Short.parseShort( stVTN.nextToken() ) - 1 );
								group_current.aiIndexTriangle[group_current.iCountTriangle] = model.iCountTriangle;
								++( model.iCountTriangle );
								++( group_current.iCountTriangle );
								while( stReadString.hasMoreTokens() )
								{ // 四角形以上対応
									stVTN = new StringTokenizer( stReadString.nextToken(), "/" );
									model.aIndexedTriangle[model.iCountTriangle].i3IndexVertex[0] = model.aIndexedTriangle[model.iCountTriangle - 1].i3IndexVertex[0];
									model.aIndexedTriangle[model.iCountTriangle].i3IndexNormal[0] = model.aIndexedTriangle[model.iCountTriangle - 1].i3IndexNormal[0];
									model.aIndexedTriangle[model.iCountTriangle].i3IndexVertex[1] = model.aIndexedTriangle[model.iCountTriangle - 1].i3IndexVertex[2];
									model.aIndexedTriangle[model.iCountTriangle].i3IndexNormal[1] = model.aIndexedTriangle[model.iCountTriangle - 1].i3IndexNormal[2];
									model.aIndexedTriangle[model.iCountTriangle].i3IndexVertex[2] = (short)( Short.parseShort( stVTN.nextToken() ) - 1 );
									stVTN.nextToken();
									model.aIndexedTriangle[model.iCountTriangle].i3IndexNormal[2] = (short)( Short.parseShort( stVTN.nextToken() ) - 1 );
									group_current.aiIndexTriangle[group_current.iCountTriangle] = model.iCountTriangle;
									++( model.iCountTriangle );
									++( group_current.iCountTriangle );
								}
							}
							else if( token.matches( "[0-9]+/[0-9]+" ) )
							{ // v/t
								StringTokenizer stVTN = new StringTokenizer( token, "/" );
								model.aIndexedTriangle[model.iCountTriangle].i3IndexVertex[0] = (short)( Short.parseShort( stVTN.nextToken() ) - 1 );
								stVTN = new StringTokenizer( stReadString.nextToken(), "/" );
								model.aIndexedTriangle[model.iCountTriangle].i3IndexVertex[1] = (short)( Short.parseShort( stVTN.nextToken() ) - 1 );
								stVTN = new StringTokenizer( stReadString.nextToken(), "/" );
								model.aIndexedTriangle[model.iCountTriangle].i3IndexVertex[2] = (short)( Short.parseShort( stVTN.nextToken() ) - 1 );
								group_current.aiIndexTriangle[group_current.iCountTriangle] = model.iCountTriangle;
								++( model.iCountTriangle );
								++( group_current.iCountTriangle );
								while( stReadString.hasMoreTokens() )
								{ // 四角形以上対応
									stVTN = new StringTokenizer( stReadString.nextToken(), "/" );
									model.aIndexedTriangle[model.iCountTriangle].i3IndexVertex[0] = model.aIndexedTriangle[model.iCountTriangle - 1].i3IndexVertex[0];
									model.aIndexedTriangle[model.iCountTriangle].i3IndexVertex[1] = model.aIndexedTriangle[model.iCountTriangle - 1].i3IndexVertex[2];
									model.aIndexedTriangle[model.iCountTriangle].i3IndexVertex[2] = (short)( Short.parseShort( stVTN.nextToken() ) - 1 );
									group_current.aiIndexTriangle[group_current.iCountTriangle] = model.iCountTriangle;
									++( model.iCountTriangle );
									++( group_current.iCountTriangle );
								}
							}
							else
							{ // v
								StringTokenizer stVTN = new StringTokenizer( token, "/" );
								model.aIndexedTriangle[model.iCountTriangle].i3IndexVertex[0] = (short)( Short.parseShort( stVTN.nextToken() ) - 1 );
								stVTN = new StringTokenizer( stReadString.nextToken(), "/" );
								model.aIndexedTriangle[model.iCountTriangle].i3IndexVertex[1] = (short)( Short.parseShort( stVTN.nextToken() ) - 1 );
								stVTN = new StringTokenizer( stReadString.nextToken(), "/" );
								model.aIndexedTriangle[model.iCountTriangle].i3IndexVertex[2] = (short)( Short.parseShort( stVTN.nextToken() ) - 1 );
								group_current.aiIndexTriangle[group_current.iCountTriangle] = model.iCountTriangle;
								++( model.iCountTriangle );
								++( group_current.iCountTriangle );
								while( stReadString.hasMoreTokens() )
								{ // 四角形以上対応
									stVTN = new StringTokenizer( stReadString.nextToken(), "/" );
									model.aIndexedTriangle[model.iCountTriangle].i3IndexVertex[0] = model.aIndexedTriangle[model.iCountTriangle - 1].i3IndexVertex[0];
									model.aIndexedTriangle[model.iCountTriangle].i3IndexVertex[1] = model.aIndexedTriangle[model.iCountTriangle - 1].i3IndexVertex[2];
									model.aIndexedTriangle[model.iCountTriangle].i3IndexVertex[2] = (short)( Short.parseShort( stVTN.nextToken() ) - 1 );
									group_current.aiIndexTriangle[group_current.iCountTriangle] = model.iCountTriangle;
									++( model.iCountTriangle );
									++( group_current.iCountTriangle );
								}
							}
						}
				}
			}

			br.close();

			return true;
		}
		catch( Exception e )
		{
			Log.e( "ObjFileLoader", "parse_second error : " + e );
			return false;
		}
	}
}
