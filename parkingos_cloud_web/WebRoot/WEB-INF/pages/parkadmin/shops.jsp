<%@ page language="java" contentType="text/html; charset=gb2312"
    pageEncoding="gb2312"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=gb2312">
<title>�̻�����</title>
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
</head>
<body>
<div id="shopobj" style="width:100%;height:100%;margin:0px;"></div>
<script language="javascript">
/*Ȩ��*/
var authlist = T.A.sendData("getdata.do?action=getauth&authid=${authid}");
var subauth=[false,false,false,false,false];
var ownsubauth=authlist.split(",");
for(var i=0;i<ownsubauth.length;i++){
	subauth[ownsubauth[i]]=true;
}
//�鿴,���,�༭,ɾ��,�޸�����
var _mediaField = [
		{fieldcnname:"���",fieldname:"id",fieldvalue:'',inputtype:"text", twidth:"100" ,height:"",issort:false,edit:false},
		{fieldcnname:"�̻�����",fieldname:"name",fieldvalue:'',inputtype:"text", twidth:"150" ,height:"",issort:false},
		{fieldcnname:"��ַ",fieldname:"address",fieldvalue:'',inputtype:"text", twidth:"130" ,height:""},
		{fieldcnname:"����ʱ��",fieldname:"create_time",fieldvalue:'',inputtype:"date", twidth:"130" ,height:"",hide:true},
		{fieldcnname:"�ֻ�",fieldname:"mobile",fieldvalue:'',inputtype:"text", twidth:"100" ,height:"",issort:false},
		{fieldcnname:"�绰",fieldname:"phone",fieldvalue:'',inputtype:"text", twidth:"100" ,height:"",issort:false},
		{fieldcnname:"�Ż�ȯ���޶�(Сʱ)",fieldname:"ticket_limit",fieldvalue:'',inputtype:"text",twidth:"200" ,height:"",issort:false},
		{fieldcnname:"ȫ��ȯ���޶�(��)",fieldname:"ticketfree_limit",fieldvalue:'',inputtype:"text",twidth:"200" ,height:"",issort:false}
	];
var _shopT = new TQTable({
	tabletitle:"�̻���ѯ",
	ischeck:false,
	tablename:"shop_tables",
	dataUrl:"shop.do",
	iscookcol:false,
	//dbuttons:false,
	buttons:getAuthButtons(),
	//searchitem:true,
	param:"action=quickquery",
	tableObj:T("#shopobj"),
	fit:[true,true,true],
	tableitems:_mediaField,
	isoperate:getAuthIsoperateButtons()
});

function getAuthButtons(){
	var bts = [];
	if(subauth[1]){
		bts.push({dname:"����̻�",icon:"edit_add.png",onpress:function(Obj){
		T.each(_shopT.tc.tableitems,function(o,j){
			o.fieldvalue ="";
		});
		Twin({Id:"shoppingmarket_add",Title:"����̻�",Width:550,sysfun:function(tObj){
				Tform({
					formname: "shoppingmarket_edit_f",
					formObj:tObj,
					recordid:"id",
					suburl:"shop.do?action=create",
					method:"POST",
					formAttr:[{
						formitems:[{kindname:"",kinditemts:_mediaField}]
					}],
					buttons : [//����
						{name: "cancel", dname: "ȡ��", tit:"ȡ�����",icon:"cancel.gif", onpress:function(){TwinC("shoppingmarket_add");} }
					],
					Callback:
					function(f,rcd,ret,o){
						if(ret=="1"){
							T.loadTip(1,"��ӳɹ���",2,"");
							TwinC("shoppingmarket_add");
							_shopT.M();
						}else{
							T.loadTip(1,ret,2,o);
						}
					}
				});	
			}
		})
	
	}});
	}
	return bts;
}
function getAuthIsoperateButtons(){
	var bts = [];
	if(subauth[2])
	bts.push({name:"�༭",fun:function(id){
		T.each(_shopT.tc.tableitems,function(o,j){
			o.fieldvalue = _shopT.GD(id)[j]
		});
		Twin({Id:"shop_edit_"+id,Title:"�༭",Width:550,sysfunI:id,sysfun:function(id,tObj){
				Tform({
					formname: "shop_edit_f",
					formObj:tObj,
					recordid:"shop_id",
					suburl:"shop.do?action=edit&id="+id,
					method:"POST",
					formAttr:[{
						formitems:[{kindname:"",kinditemts:_shopT.tc.tableitems}]
					}],
					buttons : [//����
						{name: "cancel", dname: "ȡ��", tit:"ȡ���༭",icon:"cancel.gif", onpress:function(){TwinC("shop_edit_"+id);} }
					],
					Callback:
					function(f,rcd,ret,o){
						if(ret=="1"){
							T.loadTip(1,"�༭�ɹ���",2,"");
							TwinC("shop_edit_"+id);
							_shopT.M()
						}else{
							T.loadTip(1,ret,2,o)
						}
					}
				});	
			}
		})
	}});
	if(subauth[4])
	bts.push({name:"����",fun:function(id){
		Twin({
			Id:"shop_detail_"+id,
			Title:"�̻�����  &nbsp;&nbsp;&nbsp;&nbsp;<font color='red'> ��ʾ��˫���رմ˶Ի���</font>",
			Content:"<iframe src=\"shop.do?action=setting&id="+id+"\" style=\"width:100%;height:100%\" frameborder=\"0\"></iframe>",
			Width:T.gww()-100,
			Height:T.gwh()-50
		})
	}});
	if(subauth[3])
	bts.push({name:"ɾ��",fun:function(id){
		var id_this = id ;
		Tconfirm({Title:"ȷ��ɾ����",Content:"ȷ��ɾ����",OKFn:function(){T.A.sendData("shop.do?action=delete","post","selids="+id_this,
			function deletebackfun(ret){
				if(ret=="1"){
					T.loadTip(1,"ɾ���ɹ���",2,"");
					_shopT.M()
				}else{
					T.loadTip(1,ret,2,"");
				}
			}
		)}})
	}});
	if(bts.length <= 0){return false;}
	return bts;
}
_shopT.C();
</script>

</body>
</html>
