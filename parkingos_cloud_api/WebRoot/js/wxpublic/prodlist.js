var OFFSET = 5;
var page = 1;
var PAGESIZE = 99999;
var myScroll = null;
var pullDownEl, pullDownOffset,
	pullUpEl, pullUpOffset,
	generatedCount = 0;
var maxScrollY = 0;

var hasMoreData = false;
var today = new Date();
today.setHours(0);
today.setMinutes(0);
today.setSeconds(0);
today.setMilliseconds(0);
today = today/1000 + 24*60*60;//�����ʼʱ��

document.addEventListener('touchmove', function(e) {
	e.preventDefault();
}, false);

document.addEventListener('DOMContentLoaded', function() {
	$(document).ready(function() {
//		loaded(39.946436,116.361442,'oRoektybTsv33_vSKKUwLAsJAquc');
	});
}, false);

function getparks(latitude,longitude,mobile,openid){
	$.post("getpark.do", {
		'lat' : latitude,
		'lon' : longitude,
		'action' : 'getlocal',
		'mobile' : mobile
	},
	function(response, status) {
		if (status == "success"){
			var monthids = response.monthids;
			if(monthids != undefined){
				var parkids = "";
				for(var i = 0;i<monthids.length;i++){
					if(i == 0){
						parkids = monthids[i];
					}else{
						parkids += ","+monthids[i];
					}
				}
				loaded(latitude,longitude,mobile,parkids,openid);
			}
		}
	},
	"json");
}

function loaded(latitude,longitude,mobile,monthids,openid) {
	if(myScroll != null){
		myScroll.destroy();
	}
	pullDownEl = document.getElementById('pullDown');
	pullDownOffset = pullDownEl.offsetHeight;
	pullUpEl = document.getElementById('pullUp');
	pullUpOffset = pullUpEl.offsetHeight;

	hasMoreData = false;
	page = 1;
	$.post("getpark.do", {
		'lat' : latitude,
		'lon' : longitude,
		'parkids' : monthids,
		'action' : 'getproducts',
		'mobile' : mobile
	},
	function(response, status) {
			if (status == "success") {
				$("#thelist").show();
				myScroll = new iScroll('wrapper', {
					useTransition: false,
					topOffset: pullDownOffset,
					onRefresh: function() {
						
					},
					onScrollMove: function() {
						
					},
					onScrollEnd: function() {
						
					}
				});
				if(response == null ||response.length == 0){
					document.getElementById("showinfo").innerHTML = "�����ް��³���";
					document.getElementById("imginfo").src = "images/wxpublic/tf_qrcode_close.png";
				}else{
					$("#BgDiv1").css({ display: "none", height: $(document).height() });
					$(".DialogDiv").css("display", "none");
				}
				$("#thelist").empty();
				if(response != null ||response.length > 0){
					$.each(response, function(key, value) {
						var company_name = value.company_name;
						var distance = value.distance;
						var monthProducts = value.monthProducts;
						$("#thelist").append('<li class="li3"><a href="" class="a2"><div class="first"><span class="com_cname">'+company_name+'</span></div></a><div class="border">'+distance+'km</div></li>');
						$.each(monthProducts, function(key, prod){
							var pid = prod.id;//���²�Ʒ���
							var isbuy = prod.isbuy;//�Ƿ��ѹ���
							var limitday = prod.limitday;//��������
							var limittime = prod.limittime;//����ʱ��
							var pname = prod.name;//��Ʒ����
							var number = prod.number;//ʣ������
							var price = prod.price;//�ּ�
							var type = prod.type;//-- 0:ȫ�죬1ҹ�䣬2�ռ�
							var reserved = prod.reserved;// �Ƿ�̶���λ��0���̶���1�̶�
							var photoUrl = prod.photoUrl;//ͼƬ����
							
							var picurl = "";
							if(photoUrl != undefined && photoUrl.length > 0){
								picurl = "http://s.tingchebao.com/tcbcloud/"+photoUrl[0];
							}
							var url = "wxpaccount.do?action=tobuyprod&openid="+openid+"&prodid="+pid;
							var buy = '';
							var button = "����";
							if(isbuy == 1){
								buy = '<div class="buy">�ѹ�</div>';
								button = "����";
								url +="&type=1";
							}
							$("#thelist").append('<li class="li1"><img class="img2" src="'+picurl+'" /><a href="'+url+'" class="a1"><div class="cname">'+pname+'</div><div class="distance">'+price+'Ԫ��ʣ�ࣺ'+number+'��</div></a>'+buy+'</li>');
							$("#thelist").append('<li class="li2"><a href="'+url+'" class="a2"><div class="price"><span class="first_price">ʱ</span><span class="first_cname">����ʱ�䣺'+limittime+'</span><span class="sel_fee">'+button+'</span></div></a></li>');
						});
					});
				}
				myScroll.refresh(); 
				myScroll.maxScrollY = myScroll.maxScrollY;
				maxScrollY = myScroll.maxScrollY;
			};
		},
		"json");
}
//��չDate��format����   
Date.prototype.format = function (format) {  
  var o = {  
      "M+": this.getMonth() + 1,  
      "d+": this.getDate(),  
      "h+": this.getHours(),  
      "m+": this.getMinutes(),  
      "s+": this.getSeconds(),  
      "q+": Math.floor((this.getMonth() + 3) / 3),  
      "S": this.getMilliseconds()  
  }  
  if (/(y+)/.test(format)) {  
      format = format.replace(RegExp.$1, (this.getFullYear() + "").substr(4 - RegExp.$1.length));  
  }  
  for (var k in o) {  
      if (new RegExp("(" + k + ")").test(format)) {  
          format = format.replace(RegExp.$1, RegExp.$1.length == 1 ? o[k] : ("00" + o[k]).substr(("" + o[k]).length));  
      }  
  }  
  return format;  
}

/**   
*ת��longֵΪ�����ַ���   
* @param l longֵ   
* @param isFull �Ƿ�Ϊ��������������,   
*               Ϊtrueʱ, ��ʽ��"2000-03-05 01:05:04"   
*               Ϊfalseʱ, ��ʽ�� "2000-03-05"   
* @return ����Ҫ��������ַ���   
*/    

function getSmpFormatDateByLong(l, isFull) {  
   return getSmpFormatDate(new Date(l), isFull);  
}  

/**   
*ת�����ڶ���Ϊ�����ַ���   
* @param date ���ڶ���   
* @param isFull �Ƿ�Ϊ��������������,   
*               Ϊtrueʱ, ��ʽ��"2000-03-05 01:05:04"   
*               Ϊfalseʱ, ��ʽ�� "2000-03-05"   
* @return ����Ҫ��������ַ���   
*/    
function getSmpFormatDate(date, isFull) {  
    var pattern = "";  
    if (isFull == true || isFull == undefined) {  
        pattern = "yyyy-MM-dd hh:mm:ss";  
    } else {  
        pattern = "yyyy-MM-dd";  
    }  
    return getFormatDate(date, pattern);  
} 

/**   
 *ת�����ڶ���Ϊ�����ַ���   
 * @param l longֵ   
 * @param pattern ��ʽ�ַ���,���磺yyyy-MM-dd hh:mm:ss   
 * @return ����Ҫ��������ַ���   
 */    
 function getFormatDate(date, pattern) {  
     if (date == undefined) {  
         date = new Date();  
     }  
     if (pattern == undefined) {  
         pattern = "yyyy-MM-dd hh:mm:ss";  
     }  
     return date.format(pattern);  
 }
