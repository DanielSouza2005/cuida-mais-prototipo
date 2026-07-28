import { useEffect, useState } from 'react';
import { Expand, X } from 'lucide-react-native';
import { ActivityIndicator, Image, Modal, Pressable, StyleSheet, Text, View } from 'react-native';
import { apiImageDataUrl } from '@/services/api';
import { colors, fontFamily, radii, spacing } from '@/theme/tokens';

export function CarePhoto({url}:{url:string}){
  const [source,setSource]=useState<string>();const [failed,setFailed]=useState(false);const [open,setOpen]=useState(false);
  useEffect(()=>{let active=true;setSource(undefined);setFailed(false);apiImageDataUrl(url).then(value=>{if(active)setSource(value);}).catch(()=>{if(active)setFailed(true);});return()=>{active=false;};},[url]);
  if(failed)return <View style={styles.fallback}><Text style={styles.fallbackText}>Não foi possível carregar esta foto.</Text></View>;
  if(!source)return <View style={styles.loading}><ActivityIndicator color={colors.primary}/></View>;
  return <><Pressable accessibilityRole="button" accessibilityLabel="Ampliar foto de comprovação" onPress={()=>setOpen(true)} style={styles.thumbnailWrap}><Image source={{uri:source}} style={styles.thumbnail}/><View style={styles.expand}><Expand size={14} color="#FFFFFF"/></View></Pressable><Modal visible={open} transparent animationType="fade" onRequestClose={()=>setOpen(false)}><View style={styles.backdrop}><Pressable accessibilityLabel="Fechar foto" onPress={()=>setOpen(false)} style={styles.close}><X color="#FFFFFF"/></Pressable><Image source={{uri:source}} resizeMode="contain" style={styles.full}/></View></Modal></>;
}
const styles=StyleSheet.create({thumbnailWrap:{position:'relative'},thumbnail:{width:104,height:104,borderRadius:radii.md,backgroundColor:colors.muted},expand:{position:'absolute',right:5,bottom:5,width:26,height:26,borderRadius:13,alignItems:'center',justifyContent:'center',backgroundColor:'rgba(0,0,0,.62)'},loading:{width:104,height:104,alignItems:'center',justifyContent:'center',borderRadius:radii.md,backgroundColor:colors.muted},fallback:{width:150,minHeight:104,alignItems:'center',justifyContent:'center',padding:spacing.sm,borderRadius:radii.md,backgroundColor:colors.muted},fallbackText:{fontFamily:fontFamily.medium,fontSize:11,lineHeight:16,textAlign:'center',color:colors.mutedForeground},backdrop:{flex:1,alignItems:'center',justifyContent:'center',padding:spacing.lg,backgroundColor:'rgba(0,0,0,.92)'},close:{position:'absolute',top:48,right:20,zIndex:2,width:44,height:44,alignItems:'center',justifyContent:'center',borderRadius:22,backgroundColor:'rgba(255,255,255,.18)'},full:{width:'100%',height:'86%'}});
