![](https://github.com/id-05/MagnetMonitorRemoteViewerServer/blob/master/images/preview.jpg)



# Magnet Monitor Remote Viewer

### Описание

- Программа для удаленного просмотра параметров GE Magnet Monitor 3;
- Кросплатформенное консольное приложение;
- Использует HtmlUnitDriver и эмулирует работу оператора с Web-интерфейсом;
- Прячется в трей;
- Ведёт лог опроса оборудования;
- Позволяет просматривать ошибки на устройстве;
- Предоставляет собственный Web-интерфейс для удаленного контроля;


### Как пользоваться
- На компьютере должен быть установлен Java-интерпритатор, можно скачать отсюда: https://www.java.com/ru/download/ 
- Достаточно один раз запустить Web-интерфейс на устройстве (нажать на консоли прибора кнопки: Service mode - Service mode - Yes);
- Необходимо в параметрах устройствах (кнопка Data) узнать его адрес в вашей локальной сети;
- Запустить Magnet Monitor Remote Viewer;
- Убедиться что компьютер, на котором вы запустили программу, находится в одной локальной сети с вашим устройством;
- Добавить ваше устройство с помощью формы добавления оборудования;
- Настроить интервал опроса оборудования на вкладке Settings, по умолчанию 15 минут;
- Выбрать порт для Web-интерфейса программы, по умолчанию 8765;
- Для доступа в Web-интерфейс, в адресной строке браузера набрать: http://localhost:8765 - где localhost - адрес машины на которой запущена программа; 

### Предостережение

- Для доступа к Web-интерфейсу GE Magnet Monitor 3, данная программа передает логин и пароль администратора в открытом виде, убедитесь что конфигурация вашей локальной сети, исключает возможность попадания этих данных третьим лицам;  