//
//  WelcomeViewController.m
//  WiFi IoT Node
//
//  Created by Qxn on 15/7/1.
//  Copyright © 2015年 SeeedStudio. All rights reserved.
//

#import "WelcomeViewController.h"
#import "AppDelegate.h"
#import "AFNetworking.h"

@interface WelcomeViewController () <UIScrollViewDelegate, UIGestureRecognizerDelegate>//,NSURLConnectionDelegate>
@property (weak, nonatomic) IBOutlet UIImageView *backgroundImage;
@property (weak, nonatomic) IBOutlet UIScrollView *scrollView;
@property (weak, nonatomic) IBOutlet UIPageControl *pageControl;
@property (weak, nonatomic) IBOutlet UIImageView *MaskView;
@property (weak, nonatomic) IBOutlet UIView *SignUpView;
@property (weak, nonatomic) IBOutlet UITextField *emailTextField;
@property (weak, nonatomic) IBOutlet UITextField *signUpPassTextField;
@property (weak, nonatomic) IBOutlet UITextField *signUpPassVerifyTextField;
@property (weak, nonatomic) IBOutlet NSLayoutConstraint *signUpDialogYlayout;
@property (strong, nonatomic) NSMutableData *receivedData;
@end
static NSString * const BaseURLString = @"https://iot.yuzhe.me/v1/";

@implementation WelcomeViewController
- (IBAction)signUpDoneButtonPushed{
    

    
    if ([self.emailTextField.text containsString:@"@"] &&
        [self.emailTextField.text containsString:@"."] &&
        self.emailTextField.text.length > 8)    //isValidEmail?
    {
        if ([self.signUpPassTextField.text isEqualToString:self.signUpPassVerifyTextField.text] &&
            self.signUpPassTextField.text.length > 6)   //isvalidPassword?
        {
            NSURL *baseURL = [NSURL URLWithString:BaseURLString];
            
            NSDictionary *parameters = [NSDictionary dictionaryWithObjects:@[self.emailTextField.text, self.signUpPassTextField.text] forKeys:@[@"email", @"password"]];
            AFHTTPSessionManager *manager = [[AFHTTPSessionManager alloc] initWithBaseURL:baseURL];
                        
            manager.securityPolicy.allowInvalidCertificates = YES;
            manager.responseSerializer = [AFJSONResponseSerializer serializer];
            [manager POST:@"user/create" parameters:parameters success:^(NSURLSessionDataTask *task, id responseObject) {
                
                NSString *msg = [(NSDictionary *)responseObject objectForKey:@"msg"];
                UIAlertView *alertView = [[UIAlertView alloc] initWithTitle:@"Get Server response"
                                                                    message:msg
                                                                   delegate:nil
                                                          cancelButtonTitle:@"Ok"
                                                          otherButtonTitles:nil];
                [alertView show];
                NSLog(@"JSON: %@", responseObject);
                
            } failure:^(NSURLSessionDataTask *task, NSError *error) {
                UIAlertView *alertView = [[UIAlertView alloc] initWithTitle:@"Error Retrieving Weather"
                                                                    message:[error localizedDescription]
                                                                   delegate:nil
                                                          cancelButtonTitle:@"Ok"
                                                          otherButtonTitles:nil];
                NSLog(@"Error: %@", error);
                
                [alertView show];
            }];

        } else {
            UIAlertView *alertView = [[UIAlertView alloc] initWithTitle:@"Passwords are not same"
                                                                message:nil
                                                               delegate:nil
                                                      cancelButtonTitle:@"Ok"
                                                      otherButtonTitles:nil];
            
            [alertView show];

        }
    } else {
        UIAlertView *alertView = [[UIAlertView alloc] initWithTitle:@"Please input a valid email address"
                                                            message:nil
                                                           delegate:nil
                                                  cancelButtonTitle:@"Ok"
                                                  otherButtonTitles:nil];
        
        [alertView show];

    }

}

- (IBAction)signUpCancelButtonPushed {
    
    self.emailTextField.text = nil;
    self.signUpPassTextField.text = nil;
    self.signUpPassVerifyTextField.text = nil;
    [self.emailTextField resignFirstResponder];
    [self.signUpPassTextField resignFirstResponder];
    [self.signUpPassVerifyTextField resignFirstResponder];
    
    [UIView animateWithDuration:0.5 delay:0 options:UIViewAnimationOptionCurveEaseInOut animations:^{
        self.SignUpView.alpha = 0;
        self.MaskView.alpha = 0;
    } completion:^(BOOL finished) {
        
    }];
    [UIView animateWithDuration:0.3 delay:0 options:UIViewAnimationOptionLayoutSubviews animations:^{
        //self.SignUpView.frame = CGRectOffset(self.SignUpView.frame, 0, -40);
        self.signUpDialogYlayout.constant = 0;

        self.SignUpView.center = CGPointMake(self.view.frame.size.width / 2, self.view.frame.size.height / 2);
    } completion:^(BOOL finished) {
        
        //self.emailTextField.center = CGPointMake(self.view.center.x, 100.0);
    }];

}

- (IBAction)signUpButtonPushed:(UIButton *)sender {
    
    [UIView animateWithDuration:0.3 delay:0 options:UIViewAnimationOptionCurveEaseInOut animations:^{
        self.SignUpView.alpha = 1;
        self.MaskView.alpha = 0.5;
    } completion:^(BOOL finished) {
        //
    }];
    [UIView animateWithDuration:0.3 delay:0 options:UIViewAnimationOptionLayoutSubviews animations:^{
        //self.SignUpView.frame = CGRectOffset(self.SignUpView.frame, 0, -40);
        self.signUpDialogYlayout.constant = -110;
        self.SignUpView.center = CGPointMake(self.view.center.x, self.view.center.y - 110);
    } completion:^(BOOL finished) {

        //self.emailTextField.center = CGPointMake(self.view.center.x, 100.0);
        [self.emailTextField becomeFirstResponder];
    }];
}
- (IBAction)backgroundTaped {
    [self.emailTextField resignFirstResponder];
    [self.signUpPassTextField resignFirstResponder];
    [self.signUpPassVerifyTextField resignFirstResponder];
}

- (void)viewDidLoad {
    [super viewDidLoad];
    
    self.scrollView.frame = self.view.frame;

    [self createViewOne];
    [self createViewTwo];
    [self createViewThree];
    
    self.scrollView.contentSize = CGSizeMake(self.view.frame.size.width*3, self.scrollView.frame.size.height);
    
    //This is the starting point of the ScrollView
    CGPoint scrollPoint = CGPointMake(0, 0);
    [self.scrollView setContentOffset:scrollPoint animated:YES];
    
    UITapGestureRecognizer *singleFigureOne = [[UITapGestureRecognizer alloc] initWithTarget:self action:@selector(backgroundTaped)];
    singleFigureOne.numberOfTouchesRequired = 1;
    singleFigureOne.numberOfTapsRequired = 1;
    singleFigureOne.delegate = self;
    
    [self.MaskView addGestureRecognizer:singleFigureOne];
}

- (void)maskViewTouchUpInside {
    
}
- (void)didReceiveMemoryWarning {
    [super didReceiveMemoryWarning];
    // Dispose of any resources that can be recreated.
}

-(void)createViewOne{
    
    UIView *view = [[UIView alloc] initWithFrame:self.scrollView.frame];
    
    
    UIImageView *imageview = [[UIImageView alloc] initWithFrame:CGRectMake(0, self.scrollView.frame.size.height*.1, self.scrollView.frame.size.width, self.scrollView.frame.size.height*.7)];
    imageview.contentMode = UIViewContentModeScaleAspectFill;
    imageview.image = [UIImage imageNamed:@"Open APP (intro IoT Node)"];
    [view addSubview:imageview];
    
    
    self.scrollView.delegate = self;
    [self.scrollView addSubview:view];
    
}

- (void)scrollViewDidScroll:(nonnull UIScrollView *)scrollView {
    CGFloat pageWidth = CGRectGetWidth(self.view.bounds);
    CGFloat pageFraction = self.scrollView.contentOffset.x / pageWidth;
    self.pageControl.currentPage = roundf(pageFraction);

}

-(void)createViewTwo{
    
    CGFloat originWidth = self.scrollView.frame.size.width;
    CGFloat originHeight = self.scrollView.frame.size.height;
    
    UIView *view = [[UIView alloc] initWithFrame:CGRectMake(originWidth, 0, originWidth, originHeight)];
    
    
    UIImageView *imageview = [[UIImageView alloc] initWithFrame:CGRectMake(0, self.scrollView.frame.size.height*.1, self.scrollView.frame.size.width, self.scrollView.frame.size.height*.7)];
    imageview.contentMode = UIViewContentModeScaleAspectFit;
    imageview.image = [UIImage imageNamed:@"Open APP 2 (Smart Config)"];
    [view addSubview:imageview];
    
    
    [self.scrollView addSubview:view];
    
}

-(void)createViewThree{
    
    CGFloat originWidth = self.scrollView.frame.size.width;
    CGFloat originHeight = self.scrollView.frame.size.height;
    
    UIView *view = [[UIView alloc] initWithFrame:CGRectMake(originWidth*2, 0, originWidth, originHeight)];
    
    
    UIImageView *imageview = [[UIImageView alloc] initWithFrame:CGRectMake(0, self.scrollView.frame.size.height*.1, self.scrollView.frame.size.width, self.scrollView.frame.size.height*.7)];
    imageview.contentMode = UIViewContentModeScaleAspectFit;
    imageview.image = [UIImage imageNamed:@"Open APP 3 (Node&Groves)"];
    [view addSubview:imageview];
    
    
    [self.scrollView addSubview:view];
    
}


/*
#pragma mark - Navigation

// In a storyboard-based application, you will often want to do a little preparation before navigation
- (void)prepareForSegue:(UIStoryboardSegue *)segue sender:(id)sender {
    // Get the new view controller using [segue destinationViewController].
    // Pass the selected object to the new view controller.
}
*/

@end
