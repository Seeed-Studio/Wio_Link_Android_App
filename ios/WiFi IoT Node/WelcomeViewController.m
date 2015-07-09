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
#import "NSString+Email.h"
#import "WINServiceManager.h"

static NSString * const BaseURLString = @"https://iot.yuzhe.me/v1/";



@interface WelcomeViewController () <UIScrollViewDelegate, UIGestureRecognizerDelegate, WINServiceDelegate>
@property (weak, nonatomic) IBOutlet UIImageView *backgroundImage;
@property (weak, nonatomic) IBOutlet UIScrollView *scrollView;
@property (weak, nonatomic) IBOutlet UIPageControl *pageControl;
@property (weak, nonatomic) IBOutlet UIImageView *maskView;
@property (weak, nonatomic) IBOutlet UIView *dialogView;
@property (weak, nonatomic) IBOutlet UILabel *dialogTitle;
@property (weak, nonatomic) IBOutlet UITextField *emailTextField;
@property (weak, nonatomic) IBOutlet UITextField *dialogPasswordTextField;
@property (weak, nonatomic) IBOutlet UITextField *dialogPasswordVerifyTextField;
@property (weak, nonatomic) IBOutlet NSLayoutConstraint *signUpDialogYlayout;
@property (weak, nonatomic) IBOutlet UIActivityIndicatorView *networkIndicator;
@property (weak, nonatomic) IBOutlet UIButton *dialogDoneButton;

@property (strong, nonatomic) WINServiceManager *manager;
@end



@implementation WelcomeViewController

- (WINServiceManager *)manager {
    if (_manager == nil) {
        _manager = [[WINServiceManager alloc] initWithDelegate:self];
    }
    return _manager;
}

- (void)networkIndicatorStartAnimating {
    [self.networkIndicator startAnimating];
    self.networkIndicator.hidden = NO;
    self.dialogDoneButton.userInteractionEnabled = NO;
    self.dialogDoneButton.selected = YES;
}

- (void)networkIndicatorStopAnimating {
    [self.networkIndicator stopAnimating];
    self.networkIndicator.hidden = YES;
    self.dialogDoneButton.userInteractionEnabled = YES;
    self.dialogDoneButton.selected = NO;
}

- (IBAction)dialogDoneButtonPushed{
    if (![self.emailTextField.text isEmail]) { // is not a valid email address?
        UIAlertView *alertView = [[UIAlertView alloc] initWithTitle:@"Please input a valid email address"
                                                            message:nil
                                                           delegate:nil
                                                  cancelButtonTitle:@"Ok"
                                                  otherButtonTitles:nil];
        [alertView show];
        
    } else if (![self.dialogPasswordTextField.text isEqualToString:self.dialogPasswordVerifyTextField.text] &&
               self.dialogPasswordTextField.text.length < 6)   {    //is not a validPassword?)
        UIAlertView *alertView = [[UIAlertView alloc] initWithTitle:@"The password must have more than 6 Chars, and verify field have to be same as the password."
                                                            message:nil
                                                           delegate:nil
                                                  cancelButtonTitle:@"Ok"
                                                  otherButtonTitles:nil];
        [alertView show];
        
    } else {    //All the input messages are correct, can try to send request to server

        [self networkIndicatorStartAnimating];
        if ([self.dialogTitle.text isEqualToString:@"Sign Up"]) {
            [self.manager requestSignUpNewAccountWhitEmail:self.emailTextField.text andPassword:self.dialogPasswordTextField.text];
        } else if ([self.dialogTitle.text isEqualToString:@"Sign In"]) {
            NSLog(@"Sign In request!");
        }

    }
}

- (IBAction)dialogCancelButtonPushed {
    
    [self.manager cancel];
    [self networkIndicatorStopAnimating];
    self.emailTextField.text = nil;
    self.dialogPasswordTextField.text = nil;
    self.dialogPasswordVerifyTextField.text = nil;
    [self.emailTextField resignFirstResponder];
    [self.dialogPasswordTextField resignFirstResponder];
    [self.dialogPasswordVerifyTextField resignFirstResponder];
    [self hideDialog];
}

- (void)showDialog {
    [UIView animateWithDuration:0.3 delay:0 options:UIViewAnimationOptionCurveEaseInOut animations:^{
        self.dialogView.alpha = 1;
        self.maskView.alpha = 0.5;
    } completion:^(BOOL finished) {
        //
    }];
    [UIView animateWithDuration:0.3 delay:0 options:UIViewAnimationOptionLayoutSubviews animations:^{
        self.signUpDialogYlayout.constant = -110;
        self.dialogView.center = CGPointMake(self.view.center.x, self.view.center.y - 110);
    } completion:^(BOOL finished) {
        [self.emailTextField becomeFirstResponder];
    }];
}
- (void)hideDialog {
    [UIView animateWithDuration:0.5 delay:0 options:UIViewAnimationOptionCurveEaseInOut animations:^{
        self.dialogView.alpha = 0;
        self.maskView.alpha = 0;
    } completion:^(BOOL finished) {
        
    }];
    [UIView animateWithDuration:0.3 delay:0 options:UIViewAnimationOptionLayoutSubviews animations:^{
        self.signUpDialogYlayout.constant = 0;
        self.dialogView.center = CGPointMake(self.view.frame.size.width / 2, self.view.frame.size.height / 2);
    } completion:^(BOOL finished) {
        //
    }];
}
- (IBAction)signInButtonPushed {
    self.dialogTitle.text = @"Sign In";
    self.dialogPasswordVerifyTextField.hidden = YES;
    [self.dialogDoneButton setTitle:@"Sign In" forState:UIControlStateNormal];
    [self showDialog];
}

- (IBAction)signUpButtonPushed:(UIButton *)sender {
    
    self.dialogTitle.text = @"Sign Up";
    self.dialogPasswordVerifyTextField.hidden = NO;
    [self.dialogDoneButton setTitle:@"Sign Up" forState:UIControlStateNormal];
    [self showDialog];
}
- (IBAction)backgroundTaped {
    [self.emailTextField resignFirstResponder];
    [self.dialogPasswordTextField resignFirstResponder];
    [self.dialogPasswordVerifyTextField resignFirstResponder];
}

- (void)viewDidLoad {
    [super viewDidLoad];
    
    self.scrollView.frame = self.view.frame;
    self.networkIndicator.hidden = YES;
    self.dialogView.layer.cornerRadius = 8.0;

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
    
    [self.maskView addGestureRecognizer:singleFigureOne];
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

#pragma WINServiceDelegate Method
- (void)managerRequestSignUpSucceed {
    [self networkIndicatorStopAnimating];
    UIAlertView *alertView = [[UIAlertView alloc] initWithTitle:@"Success!"
                                                        message:nil
                                                       delegate:nil
                                              cancelButtonTitle:@"Ok"
                                              otherButtonTitles:nil];
    [alertView show];

}

- (void)managerRequestSignUpFailedWithMessage:(nullable NSString *)msg {
    [self networkIndicatorStopAnimating];
    UIAlertView *alertView = [[UIAlertView alloc] initWithTitle:@"Failed!"
                                                        message:msg
                                                       delegate:nil
                                              cancelButtonTitle:@"Ok"
                                              otherButtonTitles:nil];
    [alertView show];

}
@end
