<%@ page language="java" contentType="text/html; charset=gb2312"
    pageEncoding="gb2312"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=gb2312">
<title>��Ա����</title>
<link href="css/tq.css" rel="stylesheet" type="text/css">
<link href="css/iconbuttons.css" rel="stylesheet" type="text/css">

<script src="js/tq.js?0817" type="text/javascript">//���</script>
<script src="js/tq.public.js?0817" type="text/javascript">//���</script>
<script src="js/tq.datatable.js?0817" type="text/javascript">//���</script>
<script src="js/tq.form.js?0817" type="text/javascript">//��</script>
<script src="js/tq.searchform.js?0817" type="text/javascript">//��ѯ��</script>
<script src="js/tq.window.js?0817" type="text/javascript">//����</script>
<script src="js/tq.hash.js?0817" type="text/javascript">//��ϣ</script>
<script src="js/tq.stab.js?0817" type="text/javascript">//�л�</script>
<script src="js/tq.validata.js?0817" type="text/javascript">//��֤</script>
<script src="js/My97DatePicker/WdatePicker.js" type="text/javascript">//����</script>
<script src="js/jquery.js" type="text/javascript"></script>
</head>
<body>
<div id="cityvipobj" style="width:100%;height:100%;margin:0px;"></div>
<iframe src="" id ="exportiframe" frameborder="0" style="width:0px;height:0px;"></iframe>
<form action="" method="post" id="choosecom"></form>
<script language="javascript">
var role=${role};
if(parseInt(role)==15||parseInt(role)==3){
	window.onload = jslimit()
}
/*Ȩ��*/
var authlist = T.A.sendData("getdata.do?action=getauth&authid=${authid}");
var subauth=[false,false,false,false,false];
//"�鿴,ע��,�༭,�޸ĳ���,ɾ��,����"
var ownsubauth=authlist.split(",");
for(var i=0;i<ownsubauth.length;i++){
	subauth[ownsubauth[i]]=true;
}
/*Ȩ��*/

var parks = eval(T.A.sendData("cityberthseg.do?action=getcityparks"));
var month_select = [];
for(var i=1;i<36;i++){
	month_select.push({"value_no":i,"value_name":i});
}
var _mediaField = [
		{fieldcnname:"���",fieldname:"id",fieldvalue:'',inputtype:"number", twidth:"60" ,height:"",issort:false,edit:false,hide:true,fhide:true},
		{fieldcnname:"�����ֻ�",fieldname:"mobile",fieldvalue:'',inputtype:"text", twidth:"100" ,height:"",issort:false},
		{fieldcnname:"�����˻�",fieldname:"uin",fieldvalue:'',inputtype:"number", twidth:"80" ,height:"",issort:false},
		{fieldcnname:"����",fieldname:"nickname",fieldvalue:'',inputtype:"text", twidth:"80" ,height:"",issort:false},
		{fieldcnname:"��ַ",fieldname:"address",fieldvalue:'',inputtype:"text", twidth:"180" ,height:"",issort:false},
		{fieldcnname:"���ƺ���",fieldname:"car_number",fieldvalue:'',inputtype:"text", twidth:"230" ,height:"",issort:false,edit:false,
			process:function(value,trId,colId){//ֵ����ID(��¼ID)����ID(�ֶ�����)
					return setname(trId,'car_number');
				}},
		{fieldcnname:"��������",fieldname:"comid",fieldvalue:'',inputtype:"select",noList:parks,twidth:"100" ,height:"",issort:false},
		{fieldcnname:"���һ�ι���ʱ��",fieldname:"create_time",fieldvalue:'',inputtype:"date", twidth:"140" ,height:""},
		{fieldcnname:"�¿�����ʱ��",fieldname:"e_time",fieldvalue:'',inputtype:"date", twidth:"140" ,height:"",edit:false},
		{fieldcnname:"�����¿�����(��������)",fieldname:"bcount",fieldvalue:'',inputtype:"number", twidth:"150" ,height:"",edit:false,
			process:function(value,cid,id){
					return "<a href=# onclick=\"viewdetail('"+value+"','"+cid+"')\" style='color:blue'>"+value+"</a>";
				}},
		{fieldcnname:"Ӧ�ս��ϼ�",fieldname:"atotal",fieldvalue:'',inputtype:"number", twidth:"100" ,height:"",edit:false},
		{fieldcnname:"ʵ�ս��ϼ�",fieldname:"acttotal",fieldvalue:'',inputtype:"number", twidth:"100" ,height:""}
	];
var _addField = [
		{fieldcnname:"�����ֻ�",fieldname:"mobile",fieldvalue:'',inputtype:"text", twidth:"200" ,height:"",issort:false},
		{fieldcnname:"����(ѡ��)",fieldname:"nickname",fieldvalue:'',inputtype:"text", twidth:"80" ,height:"",issort:false},
		{fieldcnname:"��ַ(ѡ��)",fieldname:"address",fieldvalue:'',inputtype:"text", twidth:"180" ,height:"",issort:false},
		{fieldcnname:"��������",fieldname:"comid",fieldvalue:'',inputtype:"cselect",noList:parks,target:"p_name",action:"getpname",twidth:"100" ,height:"",issort:false},
		{fieldcnname:"���²�Ʒ",fieldname:"p_name",fieldvalue:'',inputtype:"cselect",noList:[],action:"",target:"total",params:["months","p_name"],action:"getprodsum",twidth:"100" ,height:"",issort:false,shide:true,
			process:function(value,pid){
				return setcname(value,pid,'p_name');
			}},
		{fieldcnname:"��ʼʱ��",fieldname:"b_time",fieldvalue:'',inputtype:"sdate",twidth:"150" ,height:"",issort:false},
		{fieldcnname:"��������",fieldname:"months",fieldvalue:'',inputtype:"cselect",noList:month_select,target:"total",params:["months","p_name"],action:"getprodsum", twidth:"150" ,height:"",issort:false},
		{fieldcnname:"Ӧ�ս��",fieldname:"total",fieldvalue:'',inputtype:"text", twidth:"150" ,height:"",issort:false,edit:false},
		{fieldcnname:"ʵ�ս��",fieldname:"act_total",fieldvalue:'',inputtype:"text", twidth:"100" ,height:""},
		{fieldcnname:"��λ���",fieldname:"p_lot",fieldvalue:'',inputtype:"text", twidth:"150" ,height:"",issort:false},
		{fieldcnname:"��ע",fieldname:"remark",fieldvalue:'',inputtype:"text", twidth:"200" ,height:"",issort:false}
	];
var _editField = [
         		{fieldcnname:"����(ѡ��)",fieldname:"nickname",fieldvalue:'',inputtype:"text", twidth:"80" ,height:"",issort:false},
				{fieldcnname:"��ַ(ѡ��)",fieldname:"address",fieldvalue:'',inputtype:"text", twidth:"180" ,height:"",issort:false}
         	];
var _renewField = [
         		{fieldcnname:"�����ֻ�",fieldname:"mobile",fieldvalue:'',inputtype:"text", twidth:"200" ,height:"",issort:false,edit:false},
         		{fieldcnname:"����(ѡ��)",fieldname:"nickname",fieldvalue:'',inputtype:"text", twidth:"80" ,height:"",issort:false,edit:false},
				{fieldcnname:"��ַ(ѡ��)",fieldname:"address",fieldvalue:'',inputtype:"text", twidth:"180" ,height:"",issort:false,edit:false,fhide:true,shide:true},
         		{fieldcnname:"��������",fieldname:"comid",fieldvalue:'',inputtype:"cselect",noList:parks,target:"p_name",action:"getpname",twidth:"100" ,height:"",issort:false,edit:false},
				{fieldcnname:"���²�Ʒ",fieldname:"p_name",fieldvalue:'',inputtype:"cselect",noList:[],action:"",target:"total",params:["months","p_name"],action:"getprodsum",twidth:"100" ,height:"",issort:false,shide:true,
					process:function(value,pid){
						return setcname(value,pid,'p_name');
					}},
         		{fieldcnname:"��ʼʱ��",fieldname:"b_time",fieldvalue:'',inputtype:"sdate",twidth:"150" ,height:"",issort:false},
         		{fieldcnname:"��������",fieldname:"months",fieldvalue:'',inputtype:"cselect",noList:month_select,target:"total",params:["months","p_name"],action:"getprodsum", twidth:"150" ,height:"",issort:false},
         		{fieldcnname:"Ӧ�ս��",fieldname:"total",fieldvalue:'',inputtype:"text", twidth:"150" ,height:"",issort:false,edit:false},
				{fieldcnname:"ʵ�ս��",fieldname:"act_total",fieldvalue:'',inputtype:"text", twidth:"100" ,height:""},
         		{fieldcnname:"��λ���",fieldname:"p_lot",fieldvalue:'',inputtype:"text", twidth:"150" ,height:"",issort:false},
         		{fieldcnname:"��ע",fieldname:"remark",fieldvalue:'',inputtype:"text", twidth:"200" ,height:"",issort:false}
         	];
var rules =[{name:"p_name",type:"",url:"",requir:true,warn:"��ѡ���Ʒ",okmsg:""},
			{name:"b_time",type:"",url:"",requir:true,warn:"��ѡ��ʱ��",okmsg:""},
			{name:"mobile",type:"ajax",url:"vipuser.do?action=checkmobile&mobile=",requir:true,warn:"����д�û���ʵ�ֻ�����!",okmsg:""}
			];
var _cityvipT = new TQTable({
	tabletitle:"VIP��Ա����",
	ischeck:false,
	tablename:"cityvip_tables",
	dataUrl:"cityvip.do",
	iscookcol:false,
	//dbuttons:false,
	quikcsearch:coutomsearch(),
	buttons:getAuthButtons(),
	//searchitem:true,
	param:"action=query",
	tableObj:T("#cityvipobj"),
	fit:[true,true,true],
	tableitems:_mediaField,
	isoperate:getAuthIsoperateButtons()
});

function coutomsearch(){
	var html = "";
	return html;
}

//"�鿴,ע��,�༭,�޸ĳ���,ɾ��,����"
function getAuthButtons(){
	var authButs=[];
	if(subauth[1])
		authButs.push({dname:"ע���Ա ",icon:"edit_add.png",onpress:function(Obj){
		T.each(_cityvipT.tc.tableitems,function(o,j){
			o.fieldvalue ="";
		});
		Twin({Id:"cityvip_add",Title:"ע���Ա<font style='color:red;'></font>",Width:550,sysfun:function(tObj){
				Tform({
					formname: "cityvip_add_f",
					formObj:tObj,
					formWinId:"cityvip_add_w",
					formFunId:tObj,
					recordid:"id",
					Coltype:2,
					suburl:"cityvip.do?action=create",
					method:"POST",
					formAttr:[{
						formitems:[{kindname:"",kinditemts:_addField}],
						rules:rules
					}],
					buttons : [//����
						{name: "cancel", dname: "ȡ��", tit:"ȡ�����",icon:"cancel.gif", onpress:function(){TwinC("cityvip_add");} }
					],
					Callback:
					function(f,rcd,ret,o){
						if(ret=="1"){
							T.loadTip(1,"��ӳɹ���",2,"");
							TwinC("cityvip_add");
							_cityvipT.M();
						}else if(ret==-1){
							T.loadTip(1,"���ʧ�ܣ�",2,"");
						}else if(ret==-2){
						 	T.loadTip(2,"��Ʒ�ѳ�����Ч�ڣ�������ѡ���Ʒ����Ĺ���������",7,"");
						}else 
							T.loadTip(2,ret,7,"");
					}
				});	
			}
		})}});
	if(subauth[0])
	authButs.push({dname:"�߼���ѯ",icon:"edit_add.png",onpress:function(Obj){
		T.each(_cityvipT.tc.tableitems,function(o,j){
			o.fieldvalue ="";
		});
		Twin({Id:"cityvip_search_w",Title:"������Ա",Width:550,sysfun:function(tObj){
				TSform ({
					formname: "cityvip_search_f",
					formObj:tObj,
					formWinId:"cityvip_search_w",
					formFunId:tObj,
					formAttr:[{
						formitems:[{kindname:"",kinditemts:_mediaField}]
					}],
					buttons : [//����
						{name: "cancel", dname: "ȡ��", tit:"ȡ�����",icon:"cancel.gif", onpress:function(){TwinC("cityvip_search_w");} }
					],
					SubAction:
					function(callback,formName){
						_cityvipT.C({
							cpage:1,
							tabletitle:"�߼��������",
							extparam:"action=query&"+Serializ(formName)
						})
					}
				});	
			}
		})
	
	}});
	return authButs;
}
//"ע��,�༭,�޸ĳ���,ɾ��,����"
function getAuthIsoperateButtons(){
	var bts = [];
	if(subauth[2])
	bts.push({name:"�༭",fun:function(id){
		var uin = _cityvipT.GD(id,"uin");
		var name = _cityvipT.GD(id,"nickname");
		var address = _cityvipT.GD(id,"address");
		Twin({Id:"cityvip_edit_"+id,Title:"�༭",Width:550,sysfunI:id,sysfun:function(id,tObj){
				Tform({
					formname: "cityvip_edit_f",
					formObj:tObj,
					recordid:"cityvip_id",
					suburl:"cityvip.do?action=edit&uin="+uin,
					method:"POST",
					Coltype:2,
					formAttr:[{
						formitems:[{kindname:"",kinditemts:_editField}]
					}],
					buttons : [//����
						{name: "cancel", dname: "ȡ��", tit:"ȡ���༭",icon:"cancel.gif", onpress:function(){TwinC("cityvip_edit_"+id);} }
					],
					Callback:
					function(f,rcd,ret,o){
						if(ret=="1"){
							T.loadTip(1,"�༭�ɹ���",2,"");
							TwinC("cityvip_edit_"+id);
							_cityvipT.M();
						}else{
							T.loadTip(1,"�༭ʧ�ܣ�",2,"");
						}
					}
				});	
				T("#cityvip_edit_f_nickname").value=name;
				T("#cityvip_edit_f_address").value=address;
			}
		})
	}}); 
	 
	if(subauth[3])
	bts.push({name:"�޸ĳ���",fun:function(id){
		var uin =_cityvipT.GD(id,"uin");
		var cars = T.A.sendData("vipuser.do?action=getcar&uin="+uin);
		Twin({Id:"cityvip_addcar_"+id,Title:"�޸ĳ���",Width:450,sysfunI:id,sysfun:function(id,tObj){
				Tform({
					formname: "cityvip_addcar_f",
					formObj:tObj,
					recordid:"cityvip_id",
					suburl:"vipuser.do?action=addcar&uin="+uin,
					method:"POST",
					Coltype:2,
					formAttr:[{
						formitems:[{kindname:"",kinditemts:[{fieldcnname:"���ƺ���(������ƣ���,����)",fieldname:"carnumber",inputtype:"text",width:"300"}]}]
					}],
					buttons : [//����
						{name: "cancel", dname: "ȡ��", tit:"ȡ����ӳ���",icon:"cancel.gif", onpress:function(){TwinC("cityvip_addcar_"+id);} }
					],
					Callback:function(f,rcd,ret,o){
						if(parseInt(ret)>0){
							T.loadTip(1,"�޸���"+ret+"�����ƣ�",2,"");
							TwinC("cityvip_addcar_"+id);
							_cityvipT.M();
						}else {
							T.loadTip(2,ret,7,"");
						}
					}
				});	
			}
		})
		T("#cityvip_addcar_f_carnumber").value=cars;
	}});
	
	if(subauth[4])
	bts.push({name:"����",fun:function(id){
		var mobile = _cityvipT.GD(id,"mobile");
		var b_time = _cityvipT.GD(id,"e_time");
		var name = _cityvipT.GD(id,"nickname");
		var address = _cityvipT.GD(id,"address");
		var comid = _cityvipT.GD(id,"comid");
		
		Twin({Id:"cityvip_renew_"+id,Title:"�¿�����",Width:550,sysfunI:id,sysfun:function(id,tObj){
				Tform({
					formname: "cityvip_renew_f",
					formObj:tObj,
					recordid:"cityvip_id",
					suburl:"cityvip.do?action=renew",
					method:"POST",
					Coltype:2,
					formAttr:[{
						formitems:[{kindname:"",kinditemts:_renewField}],
						rules:rules
					}],
					buttons : [//����
						{name: "cancel", dname: "ȡ��", tit:"ȡ������",icon:"cancel.gif", onpress:function(){TwinC("cityvip_renew_"+id);} }
					],
					Callback:
					function(f,rcd,ret,o){
						if(ret=="1"){
							T.loadTip(1,"���ѳɹ���",2,"");
							TwinC("cityvip_renew_"+id);
							_cityvipT.M();
						}else if(ret==-1){
							T.loadTip(1,"����ʧ�ܣ�",2,"");
						}else if(ret==-2){
						 	T.loadTip(2,"��Ʒ�ѳ�����Ч�ڣ�������ѡ���Ʒ����Ĺ���������",7,"");
						}else 
							T.loadTip(2,ret,7,"");
					}
				});	
				T("#cityvip_renew_f_comid").value = comid;
				T("#cityvip_renew_f_mobile").value=mobile;
				T("#cityvip_renew_f_b_time").value=b_time;
				T("#cityvip_renew_f_nickname").value=name;
				T("#cityvip_renew_f_address").value=address;
				cactic_Select(comid,"cityvip_renew_f_p_name","getpname");
			}
		})
	}});
	if(bts.length <= 0){return false;}
	return bts;
}

function viewdetail(value,id){
	//alert(type+","+value);
	var comid =_cityvipT.GD(id,"comid");
	var mobile = _cityvipT.GD(id,"mobile");
	var nickname =_cityvipT.GD(id,"nickname");
	var company_name = "";
	if(parks != null && parks.length>0){
		for(var i=0; i<parks.length;i++){
			var cid = parks[i].value_no;
			if(cid == comid){
				company_name = parks[i].value_name;
				break;
			}
		}
	}
	var tip = "�����¿���¼��������"+company_name;
		
	Twin({
		Id:"vip_detail_"+id,
		Title:tip+"  --> ������"+nickname,
		Width:T.gww()-100,
		Height:T.gwh()-50,
		sysfunI:id,
		Content:"<iframe name='vip_detail_'"+id+" id='vip_detail_'"+id+" src='cityvip.do?action=detail&mobile="+mobile+"&comid="+comid+"' width='100%' height='100%' frameborder='0' scrolling='no' style='overflow:hidden;' ></iframe>"
	})
}

function setname(pid,colname){
	var uin = _cityvipT.GD(pid,"uin");
	T.A.C({
		url:"vipuser.do?action=getcar&uin="+uin,
	  		method:"GET",//POST or GET
	  		param:"",//GETʱΪ��
	  		async:false,//Ϊ��ʱ�����Ƿ��лص�����(success)�ж�
	  		dataType:"0",//0text,1xml,2obj
	  		success:function(ret,tipObj,thirdParam){
	  			if(ret&&ret!='null'){
					updateRow(pid,colname,ret);
	  			}
		},//����ɹ��ص�function(ret,tipObj,thirdParam) ret���
	  		failure:function(ret,tipObj,thirdParam){
			return false;
		},//����ʧ�ܻص�function(null,tipObj,thirdParam) Ĭ����ʾ�û�<����ʧ��>
	  		thirdParam:"",//�ص������еĵ���������
	  		tipObj:null,//�����ʾ��������(ֵΪ�ַ���"notip"ʱ��ʾ�����������ʾ)
	  		waitTip:"���ڻ�ȡ�������...",
	  		noCover:true
	})
	return "<font style='color:#666'>��ȡ��...</font>";
}

function setcname(value,pid,colname){
	if(value&&value!='-1'&&value!=''){
		T.A.C({
			url:"cityvip.do?action=getprodname&id="+value,
    		method:"GET",//POST or GET
    		param:"",//GETʱΪ��
    		async:false,//Ϊ��ʱ�����Ƿ��лص�����(success)�ж�
    		dataType:"0",//0text,1xml,2obj
    		success:function(ret,tipObj,thirdParam){
    			if(ret){
					updateRow(pid,colname,ret);
    			}
				else
					updateRow(pid,colname,value);
			},//����ɹ��ص�function(ret,tipObj,thirdParam) ret���
    		failure:function(ret,tipObj,thirdParam){
				return false;
			},//����ʧ�ܻص�function(null,tipObj,thirdParam) Ĭ����ʾ�û�<����ʧ��>
    		thirdParam:"",//�ص������еĵ���������
    		tipObj:null,//�����ʾ��������(ֵΪ�ַ���"notip"ʱ��ʾ�����������ʾ)
    		waitTip:"���ڻ�ȡ����...",
    		noCover:true
		})
	}else{
		return "��"
	};
	return "<font style='color:#666'>��ȡ��...</font>";
}

/*���±������*/
function updateRow(rowid,name,value){
	//alert(value);
	if(value)
		_cityvipT.UCD(rowid,name,value);
}

_cityvipT.C();
</script>

</body>
</html>
